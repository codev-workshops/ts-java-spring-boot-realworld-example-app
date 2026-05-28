package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  private TagDatafetcher tagDatafetcher;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    tagDatafetcher = new TagDatafetcher(tagsQueryService);
  }

  @Test
  void should_get_all_tags() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "kotlin"));

    List<String> tags = tagDatafetcher.getTags();
    assertEquals(3, tags.size());
    assertTrue(tags.contains("java"));
    assertTrue(tags.contains("spring"));
  }

  @Test
  void should_return_empty_tags() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    List<String> tags = tagDatafetcher.getTags();
    assertTrue(tags.isEmpty());
  }
}
