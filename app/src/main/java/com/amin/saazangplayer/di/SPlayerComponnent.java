package com.amin.saazangplayer.di;

import com.amin.saazangplayer.view.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {ApplicationModule.class})
//@Singleton
public interface SPlayerComponnent {
    void inject(MainActivity mainActivity);
}
