/*
 * Copyright 2016 Bartosz Lipinski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yoloo.backend.util;

/**
 * They do not extend each other on purpose.
 */
public final class Group {

    // Suppress default constructor for noninstantiability
    private Group() {
        throw new AssertionError();
    }

    public static final class OfTwo<First, Second> {
        public First first;
        public Second second;

        public OfTwo(First first, Second second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfTwo)) {
                return false;
            }
            OfTwo<?, ?> p = (OfTwo<?, ?>) o;
            return Objects.equal(p.first, first) && Objects.equal(p.second, second);
        }

        @Override
        public final int hashCode() {
            return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
        }

        public static <First, Second> OfTwo<First, Second> create(First first, Second second) {
            return new OfTwo<>(first, second);
        }
    }

    public static final class OfThree<First, Second, Third> {
        public First first;
        public Second second;
        public Third third;

        public OfThree(First first, Second second, Third third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfThree)) {
                return false;
            }
            OfThree<?, ?, ?> p = (OfThree<?, ?, ?>) o;
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

        public static <First, Second, Third> OfThree<First, Second, Third> create(
                First first, Second second, Third third) {
            return new OfThree<>(first, second, third);
        }
    }

    public static final class OfFour<First, Second, Third, Fourth> {
        public First first;
        public Second second;
        public Third third;
        public Fourth fourth;

        public OfFour(First first, Second second, Third third, Fourth fourth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfFour)) {
                return false;
            }
            OfFour<?, ?, ?, ?> p = (OfFour<?, ?, ?, ?>) o;
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

        public static <First, Second, Third, Fourth> OfFour<First, Second, Third, Fourth> create(
                First first, Second second, Third third, Fourth fourth) {
            return new OfFour<>(first, second, third, fourth);
        }
    }

    public final static class OfFive<First, Second, Third, Fourth, Fifth> {
        public First first;
        public Second second;
        public Third third;
        public Fourth fourth;
        public Fifth fifth;

        public OfFive(First first, Second second, Third third, Fourth fourth, Fifth fifth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
            this.fifth = fifth;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfFive)) {
                return false;
            }
            OfFive<?, ?, ?, ?, ?> p = (OfFive<?, ?, ?, ?, ?>) o;
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


        public static <First, Second, Third, Fourth, Fifth> OfFive<First, Second, Third, Fourth, Fifth> create(
                First first, Second second, Third third, Fourth fourth, Fifth fifth) {
            return new OfFive<>(first, second, third, fourth, fifth);
        }
    }

    public final static class OfSix<First, Second, Third, Fourth, Fifth, Sixth> {
        public First first;
        public Second second;
        public Third third;
        public Fourth fourth;
        public Fifth fifth;
        public Sixth sixth;

        public OfSix(First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
            this.fifth = fifth;
            this.sixth = sixth;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfSix)) {
                return false;
            }
            OfSix<?, ?, ?, ?, ?, ?> p = (OfSix<?, ?, ?, ?, ?, ?>) o;
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

        public static <First, Second, Third, Fourth, Fifth, Sixth> OfSix<First, Second, Third, Fourth, Fifth, Sixth> create(
                First first, Second second, Third third, Fourth fourth, Fifth fifth, Sixth sixth) {
            return new OfSix<>(first, second, third, fourth, fifth, sixth);
        }
    }

    public final static class OfSeven<First, Second, Third, Fourth, Fifth, Sixth, Seventh> {
        public First first;
        public Second second;
        public Third third;
        public Fourth fourth;
        public Fifth fifth;
        public Sixth sixth;
        public Seventh seventh;

        public OfSeven(First first, Second second, Third third, Fourth fourth, Fifth fifth,
                       Sixth sixth, Seventh seventh) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
            this.fifth = fifth;
            this.sixth = sixth;
            this.seventh = seventh;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfSeven)) {
                return false;
            }
            OfSeven<?, ?, ?, ?, ?, ?, ?> p = (OfSeven<?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equal(p.first, first) &&
                    Objects.equal(p.second, second) &&
                    Objects.equal(p.third, third) &&
                    Objects.equal(p.fourth, fourth) &&
                    Objects.equal(p.fifth, fifth) &&
                    Objects.equal(p.sixth, sixth) &&
                    Objects.equal(p.seventh, seventh);
        }

        @Override
        public final int hashCode() {
            return (first == null ? 0 : first.hashCode()) ^
                    (second == null ? 0 : second.hashCode()) ^
                    (third == null ? 0 : third.hashCode()) ^
                    (fourth == null ? 0 : fourth.hashCode()) ^
                    (fifth == null ? 0 : fifth.hashCode()) ^
                    (sixth == null ? 0 : sixth.hashCode()) ^
                    (seventh == null ? 0 : seventh.hashCode());
        }

        public static <First, Second, Third, Fourth, Fifth, Sixth, Seventh> OfSeven<First, Second, Third, Fourth, Fifth, Sixth, Seventh> create(
                First first, Second second, Third third, Fourth fourth, Fifth fifth,
                Sixth sixth, Seventh seventh) {
            return new OfSeven<>(first, second, third, fourth, fifth, sixth, seventh);
        }
    }

    public final static class OfEight<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth> {
        public First first;
        public Second second;
        public Third third;
        public Fourth fourth;
        public Fifth fifth;
        public Sixth sixth;
        public Seventh seventh;
        public Eighth eighth;

        public OfEight(First first, Second second, Third third, Fourth fourth, Fifth fifth,
                       Sixth sixth, Seventh seventh, Eighth eighth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
            this.fifth = fifth;
            this.sixth = sixth;
            this.seventh = seventh;
            this.eighth = eighth;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfEight)) {
                return false;
            }
            OfEight<?, ?, ?, ?, ?, ?, ?, ?> p = (OfEight<?, ?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equal(p.first, first) &&
                    Objects.equal(p.second, second) &&
                    Objects.equal(p.third, third) &&
                    Objects.equal(p.fourth, fourth) &&
                    Objects.equal(p.fifth, fifth) &&
                    Objects.equal(p.sixth, sixth) &&
                    Objects.equal(p.seventh, seventh) &&
                    Objects.equal(p.eighth, eighth);
        }

        @Override
        public final int hashCode() {
            return (first == null ? 0 : first.hashCode()) ^
                    (second == null ? 0 : second.hashCode()) ^
                    (third == null ? 0 : third.hashCode()) ^
                    (fourth == null ? 0 : fourth.hashCode()) ^
                    (fifth == null ? 0 : fifth.hashCode()) ^
                    (sixth == null ? 0 : sixth.hashCode()) ^
                    (seventh == null ? 0 : seventh.hashCode()) ^
                    (eighth == null ? 0 : eighth.hashCode());
        }

        public static <First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth> OfEight<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth> create(
                First first, Second second, Third third, Fourth fourth, Fifth fifth,
                Sixth sixth, Seventh seventh, Eighth eighth) {
            return new OfEight<>(first, second, third, fourth, fifth, sixth, seventh, eighth);
        }
    }

    public final static class OfNine<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth> {
        public First first;
        public Second second;
        public Third third;
        public Fourth fourth;
        public Fifth fifth;
        public Sixth sixth;
        public Seventh seventh;
        public Eighth eighth;
        public Ninth ninth;

        public OfNine(First first, Second second, Third third, Fourth fourth, Fifth fifth,
                      Sixth sixth, Seventh seventh, Eighth eighth, Ninth ninth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
            this.fifth = fifth;
            this.sixth = sixth;
            this.seventh = seventh;
            this.eighth = eighth;
            this.ninth = ninth;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfNine)) {
                return false;
            }
            OfNine<?, ?, ?, ?, ?, ?, ?, ?, ?> p = (OfNine<?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equal(p.first, first) &&
                    Objects.equal(p.second, second) &&
                    Objects.equal(p.third, third) &&
                    Objects.equal(p.fourth, fourth) &&
                    Objects.equal(p.fifth, fifth) &&
                    Objects.equal(p.sixth, sixth) &&
                    Objects.equal(p.seventh, seventh) &&
                    Objects.equal(p.eighth, eighth) &&
                    Objects.equal(p.ninth, ninth);
        }

        @Override
        public final int hashCode() {
            return (first == null ? 0 : first.hashCode()) ^
                    (second == null ? 0 : second.hashCode()) ^
                    (third == null ? 0 : third.hashCode()) ^
                    (fourth == null ? 0 : fourth.hashCode()) ^
                    (fifth == null ? 0 : fifth.hashCode()) ^
                    (sixth == null ? 0 : sixth.hashCode()) ^
                    (seventh == null ? 0 : seventh.hashCode()) ^
                    (eighth == null ? 0 : eighth.hashCode()) ^
                    (ninth == null ? 0 : ninth.hashCode());
        }

        public static <First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth> OfNine<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth> create(
                First first, Second second, Third third, Fourth fourth, Fifth fifth,
                Sixth sixth, Seventh seventh, Eighth eighth, Ninth ninth) {
            return new OfNine<>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth);
        }
    }

    public final static class OfTen<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth, Tenth> {
        public First first;
        public Second second;
        public Third third;
        public Fourth fourth;
        public Fifth fifth;
        public Sixth sixth;
        public Seventh seventh;
        public Eighth eighth;
        public Ninth ninth;
        public Tenth tenth;

        public OfTen(First first, Second second, Third third, Fourth fourth, Fifth fifth,
                     Sixth sixth, Seventh seventh, Eighth eighth, Ninth ninth, Tenth tenth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
            this.fifth = fifth;
            this.sixth = sixth;
            this.seventh = seventh;
            this.eighth = eighth;
            this.ninth = ninth;
            this.tenth = tenth;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof OfTen)) {
                return false;
            }
            OfTen<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> p = (OfTen<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equal(p.first, first) &&
                    Objects.equal(p.second, second) &&
                    Objects.equal(p.third, third) &&
                    Objects.equal(p.fourth, fourth) &&
                    Objects.equal(p.fifth, fifth) &&
                    Objects.equal(p.sixth, sixth) &&
                    Objects.equal(p.seventh, seventh) &&
                    Objects.equal(p.eighth, eighth) &&
                    Objects.equal(p.ninth, ninth) &&
                    Objects.equal(p.tenth, tenth);
        }

        @Override
        public final int hashCode() {
            return (first == null ? 0 : first.hashCode()) ^
                    (second == null ? 0 : second.hashCode()) ^
                    (third == null ? 0 : third.hashCode()) ^
                    (fourth == null ? 0 : fourth.hashCode()) ^
                    (fifth == null ? 0 : fifth.hashCode()) ^
                    (sixth == null ? 0 : sixth.hashCode()) ^
                    (seventh == null ? 0 : seventh.hashCode()) ^
                    (eighth == null ? 0 : eighth.hashCode()) ^
                    (ninth == null ? 0 : ninth.hashCode()) ^
                    (tenth == null ? 0 : tenth.hashCode());
        }

        public static <First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth, Tenth> OfTen<First, Second, Third, Fourth, Fifth, Sixth, Seventh, Eighth, Ninth, Tenth> create(
                First first, Second second, Third third, Fourth fourth, Fifth fifth,
                Sixth sixth, Seventh seventh, Eighth eighth, Ninth ninth, Tenth tenth) {
            return new OfTen<>(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
        }
    }

    private static final class Objects {
        public static boolean equal(Object a, Object b) {
            return a == b || (a != null && a.equals(b));
        }
    }
}