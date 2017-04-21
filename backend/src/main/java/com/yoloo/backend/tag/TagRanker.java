package com.yoloo.backend.tag;

import java.util.List;

public class TagRanker {
  // The rate at which the historic data's effect will diminish
  private float decay;
  private List<Tag> population;
}
