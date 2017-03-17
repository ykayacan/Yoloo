package com.yoloo.android.util;

/*
 * Copyright 2016 Bartosz Lipinski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy getPost the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * They do not extend each other on purpose.
 */
public final class Group {

  private Group() {
    throw new AssertionError();
  }

  public static final class Of3<First, Second, Third> {
    public First first;
    public Second second;
    public Third third;

    public Of3(First first, Second second, Third third) {
      this.first = first;
      this.second = second;
      this.third = third;
    }

    public static <First, Second, Third> Of3<First, Second, Third> create(
        First first, Second second, Third third) {
      return new Of3<>(first, second, third);
    }

    @Override
    public final boolean equals(Object o) {
      if (!(o instanceof Of3)) {
        return false;
      }
      Of3<?, ?, ?> p = (Of3<?, ?, ?>) o;
      return Objects.equal(p.first, first) &&
          Objects.equal(p.second, second) &&
          Objects.equal(p.third, third);
    }

    @Override
    public final int hashCode() {
      return (first == null ? 0 : first.hashCode()) ^
          (second == null ? 0 : second.hashCode()) ^
          (third == null ? 0 : third.hashCode());
    }
  }

  public static final class Of4<First, Second, Third, Fourth> {
    public First first;
    public Second second;
    public Third third;
    public Fourth fourth;

    public Of4(First first, Second second, Third third, Fourth fourth) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
    }

    public static <First, Second, Third, Fourth> Of4<First, Second, Third, Fourth> create(
        First first, Second second, Third third, Fourth fourth) {
      return new Of4<>(first, second, third, fourth);
    }

    @Override
    public final boolean equals(Object o) {
      if (!(o instanceof Of4)) {
        return false;
      }
      Of4<?, ?, ?, ?> p = (Of4<?, ?, ?, ?>) o;
      return Objects.equal(p.first, first) &&
          Objects.equal(p.second, second) &&
          Objects.equal(p.third, third) &&
          Objects.equal(p.fourth, fourth);
    }

    @Override
    public final int hashCode() {
      return (first == null ? 0 : first.hashCode()) ^
          (second == null ? 0 : second.hashCode()) ^
          (third == null ? 0 : third.hashCode()) ^
          (fourth == null ? 0 : fourth.hashCode());
    }
  }

  public final static class Of5<First, Second, Third, Fourth, Fifth> {
    public First first;
    public Second second;
    public Third third;
    public Fourth fourth;
    public Fifth fifth;

    public Of5(First first, Second second, Third third, Fourth fourth, Fifth fifth) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
      this.fifth = fifth;
    }

    public static <First, Second, Third, Fourth, Fifth> Of5<First, Second, Third, Fourth, Fifth> create(
        First first, Second second, Third third, Fourth fourth, Fifth fifth) {
      return new Of5<>(first, second, third, fourth, fifth);
    }

    @Override
    public final boolean equals(Object o) {
      if (!(o instanceof Of5)) {
        return false;
      }
      Of5<?, ?, ?, ?, ?> p = (Of5<?, ?, ?, ?, ?>) o;
      return Objects.equal(p.first, first) &&
          Objects.equal(p.second, second) &&
          Objects.equal(p.third, third) &&
          Objects.equal(p.fourth, fourth) &&
          Objects.equal(p.fifth, fifth);
    }

    @Override
    public final int hashCode() {
      return (first == null ? 0 : first.hashCode()) ^
          (second == null ? 0 : second.hashCode()) ^
          (third == null ? 0 : third.hashCode()) ^
          (fourth == null ? 0 : fourth.hashCode()) ^
          (fifth == null ? 0 : fifth.hashCode());
    }
  }

  public final static class Of6<First, Second, Third, Fourth, Fifth, Sixth> {
    public First first;
    public Second second;
    public Third third;
    public Fourth fourth;
    public Fifth fifth;
    public Sixth sixth;

    public Of6(First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
      this.fifth = fifth;
      this.sixth = sixth;
    }

    public static <First, Second, Third, Fourth, Fifth, Sixth> Of6<First, Second, Third, Fourth, Fifth, Sixth> create(
        First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth) {
      return new Of6<>(first, second, third, fourth, fifth, sixth);
    }

    @Override
    public final boolean equals(Object o) {
      if (!(o instanceof Of6)) {
        return false;
      }
      Of6<?, ?, ?, ?, ?, ?> p = (Of6<?, ?, ?, ?, ?, ?>) o;
      return Objects.equal(p.first, first) &&
          Objects.equal(p.second, second) &&
          Objects.equal(p.third, third) &&
          Objects.equal(p.fourth, fourth) &&
          Objects.equal(p.fifth, fifth) &&
          Objects.equal(p.sixth, sixth);
    }

    @Override
    public final int hashCode() {
      return (first == null ? 0 : first.hashCode()) ^
          (second == null ? 0 : second.hashCode()) ^
          (third == null ? 0 : third.hashCode()) ^
          (fourth == null ? 0 : fourth.hashCode()) ^
          (fifth == null ? 0 : fifth.hashCode()) ^
          (sixth == null ? 0 : sixth.hashCode());
    }
  }

  private static final class Objects {
    public static boolean equal(Object a, Object b) {
      return a == b || (a != null && a.equals(b));
    }
  }
}
