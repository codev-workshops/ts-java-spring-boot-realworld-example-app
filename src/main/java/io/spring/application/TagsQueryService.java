package io.spring.application;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Read-side application service for article tags. */
@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagReadService tagReadService;

  /**
   * Lists the names of all tags currently attached to at least one article.
   *
   * @return the tag names, possibly empty; never {@code null}
   */
  public List<String> allTags() {
    return tagReadService.all();
  }
}
