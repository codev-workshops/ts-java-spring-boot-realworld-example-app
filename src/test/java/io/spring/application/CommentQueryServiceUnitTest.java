package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CommentQueryServiceUnitTest {

  private CommentReadService commentReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private CommentQueryService commentQueryService;

  @BeforeEach
  public void setUp() {
    commentReadService = Mockito.mock(CommentReadService.class);
    userRelationshipQueryService = Mockito.mock(UserRelationshipQueryService.class);
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
  }

  @Test
  public void testFindByIdReturnsEmptyWhenNotFound() {
    when(commentReadService.findById("nonexistent")).thenReturn(null);
    User user = new User("a@test.com", "a", "123", "", "");
    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByIdReturnsDataWhenFound() {
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    CommentData comment =
        new CommentData("c1", "body", "art1", new DateTime(), new DateTime(), profile);
    when(commentReadService.findById("c1")).thenReturn(comment);
    when(userRelationshipQueryService.isUserFollowing(anyString(), eq("authorId"))).thenReturn(true);

    User user = new User("a@test.com", "a", "123", "", "");
    Optional<CommentData> result = commentQueryService.findById("c1", user);
    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void testFindByArticleIdWithNullUser() {
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    CommentData comment =
        new CommentData("c1", "body", "art1", new DateTime(), new DateTime(), profile);
    when(commentReadService.findByArticleId("art1")).thenReturn(Arrays.asList(comment));

    List<CommentData> result = commentQueryService.findByArticleId("art1", null);
    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  public void testFindByArticleIdWithEmptyComments() {
    when(commentReadService.findByArticleId("art1")).thenReturn(new ArrayList<>());
    User user = new User("a@test.com", "a", "123", "", "");
    List<CommentData> result = commentQueryService.findByArticleId("art1", user);
    assertEquals(0, result.size());
  }

  @Test
  public void testFindByArticleIdWithUserFollowing() {
    ProfileData profile1 = new ProfileData("author1", "auth1", "bio", "img", false);
    ProfileData profile2 = new ProfileData("author2", "auth2", "bio", "img", false);
    CommentData comment1 =
        new CommentData("c1", "body1", "art1", new DateTime(), new DateTime(), profile1);
    CommentData comment2 =
        new CommentData("c2", "body2", "art1", new DateTime(), new DateTime(), profile2);
    when(commentReadService.findByArticleId("art1")).thenReturn(Arrays.asList(comment1, comment2));
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author1")));

    User user = new User("u@test.com", "u", "123", "", "");
    List<CommentData> result = commentQueryService.findByArticleId("art1", user);
    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  public void testFindByArticleIdWithCursorEmptyComments() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor("art1", page))
        .thenReturn(new ArrayList<>());

    User user = new User("a@test.com", "a", "123", "", "");
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("art1", user, page);
    assertEquals(0, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  public void testFindByArticleIdWithCursorNullUser() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 2, Direction.NEXT);
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData comment =
        new CommentData("c1", "body", "art1", new DateTime(1000L), new DateTime(2000L), profile);
    when(commentReadService.findByArticleIdWithCursor("art1", page))
        .thenReturn(new ArrayList<>(Arrays.asList(comment)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("art1", null, page);
    assertEquals(1, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  public void testFindByArticleIdWithCursorHasExtra() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData comment1 =
        new CommentData("c1", "body1", "art1", new DateTime(1000L), new DateTime(2000L), profile);
    CommentData comment2 =
        new CommentData("c2", "body2", "art1", new DateTime(3000L), new DateTime(4000L), profile);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(comment1, comment2));
    when(commentReadService.findByArticleIdWithCursor("art1", page)).thenReturn(comments);

    User user = new User("u@test.com", "u", "123", "", "");
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("art1", user, page);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  public void testFindByArticleIdWithCursorPrevDirection() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 2, Direction.PREV);
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData comment1 =
        new CommentData("c1", "body1", "art1", new DateTime(1000L), new DateTime(2000L), profile);
    CommentData comment2 =
        new CommentData("c2", "body2", "art1", new DateTime(3000L), new DateTime(4000L), profile);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(comment1, comment2));
    when(commentReadService.findByArticleIdWithCursor("art1", page)).thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("art1", null, page);
    assertEquals(2, result.getData().size());
    assertEquals("c2", result.getData().get(0).getId());
    assertEquals("c1", result.getData().get(1).getId());
  }

  @Test
  public void testFindByArticleIdWithCursorUserFollowing() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData comment =
        new CommentData("c1", "body", "art1", new DateTime(1000L), new DateTime(2000L), profile);
    when(commentReadService.findByArticleIdWithCursor("art1", page))
        .thenReturn(new ArrayList<>(Arrays.asList(comment)));
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author1")));

    User user = new User("u@test.com", "u", "123", "", "");
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("art1", user, page);
    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }
}
