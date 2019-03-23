# ViewSwitcher [![](https://jitpack.io/v/fiberthemax/ViewSwitcher.svg)](https://jitpack.io/#fiberthemax/ViewSwitcher)
Similar to ViewPager. but can not be swiped and has no off-screen pages.

Usage
--------
Use it the same way as ViewPager.
```java
   ViewSwitcher viewSwitcher = findViewById(R.id.viewSwitcher);
   viewSwitcher.setAdapter(new TextFragmentPagerAdapter(getSupportFragmentManager()));
   viewSwitcher.addOnPageChangeListener(new ViewSwitcher.OnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
      
      }
    });
```
Download
--------
```groovy
   repositories {
        jcenter()
        maven { url "https://jitpack.io" }
   }
   dependencies {
        implementation 'com.github.fiberthemax:ViewSwitcher:v0.5.0'
   }
```

License
-------

    Copyright 2018 fiberthemax

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
