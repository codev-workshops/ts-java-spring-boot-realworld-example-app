package io.spring.application.comment;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  MyBatisCommentRepository.class,
  MyBatisUserRepository.class,
  CommentQueryService.class,
  MyBatisArticleRepository.class
})
public class CommentQueryServiceTest extends DbTestBase {
  @Autowired private CommentRepository commentRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CommentQueryService commentQueryService;

  @Autowired private ArticleRepository articleRepository;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
    userRepository.save(user);
  }

  @Test
  public void should_read_comment_success() {
    Comment comment = new Comment("content", user.getId(), "123");
    commentRepository.save(comment);

    Optional<CommentData> optional = commentQueryService.findById(comment.getId(), user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);

    User user2 = new User("user2@email.com", "user2", "123", "", "");
    userRepository.save(user2);
    userRepository.saveRelation(new FollowRelation(user.getId(), user2.getId()));

    Comment comment1 = new Comment("content1", user.getId(), article.getId());
    commentRepository.save(comment1);
    Comment comment2 = new Comment("content2", user2.getId(), article.getId());
    commentRepository.save(comment2);

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
    Assertions.assertEquals(comments.size(), 2);
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    Optional<CommentData> optional = commentQueryService.findById("not-exists", user);
    Assertions.assertFalse(optional.isPresent());
  }

  @Test
  public void should_return_empty_list_when_article_has_no_comments() {
    List<CommentData> comments = commentQueryService.findByArticleId("no-article", user);
    Assertions.assertTrue(comments.isEmpty());
  }

  @Test
  public void should_read_comments_of_article_with_null_user() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);
    commentRepository.save(new Comment("content", user.getId(), article.getId()));

    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), null);
    Assertions.assertEquals(1, comments.size());
    Assertions.assertFalse(comments.get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_cursor_page_when_no_comments() {
    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            "no-article", user, new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertTrue(page.getData().isEmpty());
    Assertions.assertFalse(page.hasNext());
  }

  @Test
  public void should_read_comments_with_cursor_and_following_flag() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);

    User author = new User("author@test.com", "author", "123", "", "");
    userRepository.save(author);
    userRepository.saveRelation(new FollowRelation(user.getId(), author.getId()));
    commentRepository.save(new Comment("content", author.getId(), article.getId()));

    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), user, new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertEquals(1, page.getData().size());
    Assertions.assertTrue(page.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_read_comments_with_cursor_has_extra_and_prev_direction() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);

    for (int i = 0; i < 3; i++) {
      commentRepository.save(new Comment("content" + i, user.getId(), article.getId()));
    }

    CursorPager<CommentData> next =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), user, new CursorPageParameter<>(null, 2, Direction.NEXT));
    Assertions.assertEquals(2, next.getData().size());
    Assertions.assertTrue(next.hasNext());

    CursorPager<CommentData> prev =
        commentQueryService.findByArticleIdWithCursor(
            article.getId(), user, new CursorPageParameter<>(new DateTime(0), 2, Direction.PREV));
    Assertions.assertEquals(2, prev.getData().size());
    Assertions.assertTrue(prev.hasPrevious());
  }
}
