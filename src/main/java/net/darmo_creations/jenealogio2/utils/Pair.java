package net.darmo_creations.jenealogio2.utils;

/**
 * A class representing a pair of values.
 *
 * @param left  Left value.
 * @param right Right value.
 * @param <T1>  Left value’s type.
 * @param <T2>  Right values’s type.
 */
public record Pair<T1, T2>(T1 left, T2 right) {
}
