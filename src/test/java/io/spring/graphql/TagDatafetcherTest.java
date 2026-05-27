package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  private TagDatafetcher tagDatafetcher;

  @BeforeEach
  void setUp() {
    tagDatafetcher = new TagDatafetcher(tagsQueryService);
  }

  @Test
  void getTags_returns_list() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "graphql"));

    List<String> result = tagDatafetcher.getTags();

    assertEquals(3, result.size());
    assertEquals("java", result.get(0));
    assertEquals("spring", result.get(1));
    assertEquals("graphql", result.get(2));
  }

  @Test
  void getTags_returns_empty_list() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList());

    List<String> result = tagDatafetcher.getTags();

    assertTrue(result.isEmpty());
  }
}
