# **SwipeTransitionView**


---
A lightweight XML‑driven swipe transition library for Android that lets you create a smooth, gesture‑based image gallery with zero mandatory Kotlin logic.

---

## ✨ **Features**

-Swipe images horizontally, vertically, or in all directions

- Smooth transition animations

- Optional overlay & swipe indicators

- Infinite image looping (last → first)

- Works as a standalone custom View



  ---

# **Preview**
---
<p align="center">
  <img src="https://github.com/S13reya/Android_Zoom_Gesture_Image_View/blob/stages/app/src/main/assets/demovideo.gif" height="320"/>




</p>


## ⚡ **Installation**

**Step 1:** Add JitPack repository to your root build.gradle:

```gradle
maven { url = uri("https://jitpack.io") }
```

**Step 2:** Add the dependency in your app `build.gradle` (example if hosted on JitPack):  

```gradle
dependencies {
	        implementation 'com.github.Excelsior-Technologies-Community:Android_Zoom_Gesture_Image_View:1.0.0'

}
```
## ⚡ **attrs file**

```

<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="SwipeTransitionView">

        <!-- Swipe direction: horizontal, vertical, or all -->
        <attr name="swipeDirection" format="enum">
            <enum name="horizontal" value="0" />
            <enum name="vertical" value="1" />
            <enum name="all" value="2" />
        </attr>

        <!-- Duration of transition animation in milliseconds -->
        <attr name="transitionDuration" format="integer" />

        <!-- Threshold for swipe completion (0.0 to 1.0) -->
        <attr name="swipeThreshold" format="float" />

        <!-- Enable or disable swipe gesture -->
        <attr name="enableSwipe" format="boolean" />

        <!-- Overlay color during swipe -->
        <attr name="overlayColor" format="color" />

        <!-- Show swipe direction indicator -->
        <attr name="showIndicator" format="boolean" />

        <!-- Color of the swipe indicator -->
        <attr name="indicatorColor" format="color" />

        <!-- Size of the swipe indicator -->
        <attr name="indicatorSize" format="dimension" />

        <!-- Elevation/shadow depth during swipe -->
        <attr name="swipeElevation" format="dimension" />

    </declare-styleable>
</resources>

```

## ⚡ **Usage**

1. Add in XML

```
  <com.ext.android_swipe_transition.SwipeTransitionView
        android:id="@+id/swipeView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_margin="24dp"
        android:background="#FFFFFF"
        android:elevation="8dp"
        app:swipeDirection="all"
        app:transitionDuration="300"
        app:swipeThreshold="0.3"
        app:enableSwipe="true"
        app:overlayColor="#40000000"
        app:showIndicator="true"
        app:indicatorColor="#FFFFFF"
        app:indicatorSize="70dp"
        app:swipeElevation="24dp"
        app:layout_constraintTop_toBottomOf="@id/counterText"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
```





## **📄 License**

**MIT License**  
```
Copyright (c) 2025 Excelsior Technologies

Permission is hereby granted, free of charge, to any person obtaining a copy  
of this software and associated documentation files (the "Software"), to deal  
in the Software without restriction, including without limitation the rights  
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
copies of the Software, and to permit persons to whom the Software is  
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all  
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED **"AS IS"**, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```



  
