package com.yoloo.backend.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Ref;
import java.util.List;

public class Deref {

  public static <T> T deref(Ref<T> ref) {
    return ref == null ? null : ref.getValue();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> List<T> deref(List<Ref<T>> reflist) {
    return Lists.transform(reflist, (Func) Func.INSTANCE);
  }

  private static class Func<T> implements Function<Ref<T>, T> {
    static Func<Object> INSTANCE = new Func<>();

    @Override
    public T apply(Ref<T> ref) {
      return deref(ref);
    }
  }
}
