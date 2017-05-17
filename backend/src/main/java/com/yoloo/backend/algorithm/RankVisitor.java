package com.yoloo.backend.algorithm;

import com.yoloo.backend.post.PostEntity;

public interface RankVisitor {

  void visit(PostEntity post);

  class TrendingRankVisitor implements RankVisitor {

    @Override public void visit(PostEntity post) {
    }
  }
}
