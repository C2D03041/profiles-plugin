package com.awooga.profiles.chestgui;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ChestGuiGenerator {
    @Inject
    Injector injector;

    public <S> ChestGui<S> createNewGui(String configKey, StatefulItemStackSupplier<S> supplier) {
        ChestGui<S> chestGui = ChestGui.<S>builder()
            .supplier(supplier)
            .configKey(configKey)
        .build();

        this.injector.injectMembers(chestGui);
        return chestGui;
    }
}
