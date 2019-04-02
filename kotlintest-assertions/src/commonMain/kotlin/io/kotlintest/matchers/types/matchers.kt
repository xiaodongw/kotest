package io.kotlintest.matchers.types

import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.neverNullMatcher
import io.kotlintest.platformQualifiedName
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNot
import io.kotlintest.shouldNotBe
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * Verifies that this is instanceof T
 *
 * Verifies that this value is an instance of T, which include any subclasses, and lets you execute [block] with that
 * value casted.
 *
 * Opposite of [shouldNotBeInstanceOf]
 *
 * For an exact type, use [shouldBeTypeOf]
 *
 * ```
 *
 * val list: List<Int> = arraylistOf(1, 2, 3)
 *
 * list.shouldBeInstanceOf<ArrayList<Int>> { it: ArrayList<Int> // Typecasted for you if not explicit
 *  // Use it.
 * }
 *
 * ```
 */
inline fun <reified T : Any> Any?.shouldBeInstanceOf(block: (T) -> Unit = { }) {
  val matcher = beInstanceOf<T>()
  this shouldBe matcher
  block(this as T)
}

/**
 * Verifies that this is NOT Instanceof T
 *
 * Verifies that this value is not an instance of T.
 *
 * Opposite of [shouldBeInstanceOf]
 *
 * For an exact type, use [shouldNotBeTypeOf]
 *
 * ```
 * val list: List<Int> = arrayListOf(1, 2, 3)
 *
 * list.shouldNotBeInstanceOf<LinkedList<Int>>
 * ```
 */
inline fun <reified T : Any> Any?.shouldNotBeInstanceOf() {
  val matcher = beInstanceOf<T>()
  this shouldNotBe matcher
}

/**
 * Verifies that this is exactly of type T
 *
 * Verifies that this value is exactly of type T, where no inheritance is verified. If the assertion passes, you may
 * use [this] as T inside [block].
 *
 * Opposite of [shouldNotBeTypeOf]
 *
 * If you want to verify including inheritance, use [shouldBeInstanceOf]
 *
 * ```
 * val list: List<Int> = arrayListOf(1, 2, 3)
 *
 * list.shouldBeTypeOf<ArrayList<Int>> { it: ArrayList<Int> // Typecasted for you if not explicit
 * // Use it
 * }
 * ```
 */
inline fun <reified T : Any> Any?.shouldBeTypeOf(block: (T) -> Unit = { }) {
  val matcher = beOfType<T>()
  this shouldBe matcher
  block(this as T)
}

/**
 * Verifies that this is NOT exactly of type T
 *
 * Verifies that this value is not of type T.
 *
 * Opposite of [shouldBeTypeOf]
 *
 * If you want to consider inheritance, use [shouldNotBeInstanceOf]
 *
 * ```
 * val list: List<Int> = arrayListOf(1, 2, 3)
 *
 * list.shouldNotBeTypeOf<LinkedList<Int>>
 * ```
 */
inline fun <reified T : Any> Any?.shouldNotBeTypeOf() {
  val matcher = beOfType<T>()
  this shouldNotBe matcher
}

infix fun Any?.shouldBeSameInstanceAs(ref: Any?) = this should beTheSameInstanceAs(ref)
infix fun Any?.shouldNotBeSameInstanceAs(ref: Any?) = this shouldNotBe beTheSameInstanceAs(ref)

/**
 * Verifies that this value is null
 *
 * Matcher to verify that a specific value contains a reference to `null`.
 * Opposite of [shouldNotBeNull]
 *
 * Example:
 *
 * ```
 *     val nullable: String? = null
 *     val nonNull: String? = "NonNull"
 *
 *     nullable.shouldBeNull()    // Passes
 *     nonNull.shouldBeNull()     // Fails
 *
 * ```
 */
@UseExperimental(ExperimentalContracts::class)
fun Any?.shouldBeNull() {
  contract {
    returns() implies (this@shouldBeNull == null)
  }

  this should beNull()
}

/**
 * Verifies that this is not null
 *
 * Matcher to verify that a specific nullable reference is not null.
 * Opposite of [shouldBeNull]
 *
 * Example:
 *
 * ```
 *     val nullable: String? = null
 *     val nonNull: String? = "NonNull"
 *
 *     nonNull.shouldNotBeNull()     // Passes
 *     nullable.shouldNotBeNull()    // Fails
 * ```
 *
 * Note: This function uses Kotlin Contracts to tell the compiler that this is not null. So after this is used, all subsequent
 * lines can assume the value is not null without having to cast it. For example:
 *
 * ```
 *
 *     val nonNull: String? = "NonNull"
 *
 *     nonNull.shouldNotBeNull()
 *     useNonNullString(nonNull)
 *
 *
 *     // Notice how this is a not-nullable reference
 *     fun useNonNullString(string: String) { }
 *
 * ```
 */
@UseExperimental(ExperimentalContracts::class)
fun Any?.shouldNotBeNull() {
  contract {
    returns() implies (this@shouldNotBeNull != null)
  }

  this shouldNot beNull()
}


/**
 * Matcher that verifies if a reference is null
 *
 * Verifies that a given value contains a reference to null or not.
 *
 * Example:
 * ```
 *     val nullable: String? = null
 *     val nonNull: String? = "NonNull"
 *
 *     nullable should beNull() // Passes
 *     nonNull should beNull()  // Fails
 *
 *     nullable shouldNot beNull() // Fails
 *     nonNull shouldNot beNull()  // Passes
 *
 * ```
 * @see [shouldBeNull]
 * @see [shouldNotBeNull]
 */
fun beNull() = object : Matcher<Any?> {

  override fun test(value: Any?): Result {
    val passed = value == null

    return Result(passed,
        "Expected value to be null, but was not-null.",
        "Expected value to not be null, but was null.")
  }

}


// alias for beInstanceOf
fun instanceOf(expected: KClass<*>): Matcher<Any?> = beInstanceOf(expected)

fun beInstanceOf(expected: KClass<*>): Matcher<Any?> = neverNullMatcher { value ->
  Result(
          value.isSubclassOf(expected),
          "$value is of type ${value::class} but expected $expected",
          "$value should not be of type $expected"
  )
}

fun <T> beTheSameInstanceAs(ref: T): Matcher<T> = object : Matcher<T> {
  override fun test(value: T) = Result(value === ref,
          "$value should be the same reference as $ref",
          "$value should not be the same reference as $ref")
}

inline fun <reified U : Any, reified T : U> beInstanceOf2(): Matcher<U> = object : Matcher<U> {
  
  override fun test(value: U): Result =
          Result(
                  value.isSubclassOf(T::class),
                  "$value is of type ${value::class} but expected ${T::class.platformQualifiedName}",
                  "$value should not be an instance of ${T::class.platformQualifiedName}")
  
}


// checks that the given value is an instance (of type or of subtype) of T
inline fun <reified T : Any> beInstanceOf(): Matcher<Any?> = beInstanceOf(T::class)

fun beOfType(expected: KClass<*>): Matcher<Any?> = neverNullMatcher { value ->
  Result(
          value::class == expected,
          "$value should be of type ${expected.platformQualifiedName}",
          "$value should not be of type ${expected.platformQualifiedName}")
}

inline fun <reified T : Any> beOfType(): Matcher<Any?> = beOfType(T::class)


@PublishedApi
internal expect inline fun <reified T : Any> T.isSubclassOf(klass: KClass<*>): Boolean