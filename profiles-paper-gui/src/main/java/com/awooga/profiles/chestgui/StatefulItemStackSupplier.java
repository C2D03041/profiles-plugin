package com.awooga.profiles.chestgui;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface StatefulItemStackSupplier<S> {
    StatefulItemStack<S> get(Integer slot, String legendName, TextSupplier getText);
}
