# Welcome to Panache!
Panache loads images from the web and displays them in a ListView. It supports asynchronous loading of images, loads a scaled down version of the image to improve download speed, handles multithreading complexities introduced by the ListView and uses both memory and disk caching to speed things up.

![Screenshot](/res/drawable/panache.png "Screenshot")

It does not use any third party library such as the [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader) to do this. 

**Things I like about this app:**

1. Panache loads scaled down versions of images into memory. It calculates the dimension of the image required to display it on the ListView, and then downloads a scaled down version, reducing unnecessary  overheads. 

2. Memory and Disk caching is used to improve the scrolling experience. The scaled down images are cached, and when you scroll up and down once the images have downloaded the first time, the images don't flicker and the scrolling is really smooth.

3. Panache uses asynchronous loading of images, and the images are processed off the UI thread such that the UI remains responsive throughout the download.

4. When components such as the ListView or GridView are used along with asynchronous tasks, there are some multithreading issues that are introduced. For example, the ListView recycles the child views every time the user scrolls, causing several images to appear in the same ImageView. Panache handles these concurrency issues by remembering the order of the image downloads, and only the image from the last download is displayed for a given ImageView. 

5. Custom ActionBar color, title and launcher icons. "Long pressing" an item on the ListView displays a color that matches the ActionBar. The ListView divider also has the same green color. I especially like the Panache logo that was chosen keeping the spirit of Movember in mind!

**What can be improved:**

1. The UI can be improved by using larger images, and better formatting of text for the image names.

2. It would be better to parse the image names out of the URL itself. I now cheat a bit and include it separately.

3. Add more error handling for cases when there is no connection to the Internet, or when downloading an image fails due to a broken url.

4. Ensure that the phone doesn't run out of memory or disk space while caching several large images. 

5. Make sure that the caches are being freed once the app is quit. 


**Testing the app:**

1. Test for OutOfMemory errors thrown by the memory cache by downloading images of very large size. Do the same to test the boundaries of the disk cache. 

2. Test runtime configuration changes such as change in phone orientation while the app is running.

3. Test for running out of disk space by downloading long list of large images.

4. Test how the app behaves when the phone is not connected to the Internet. 

5. User interaction testing to see if the app correctly responds to events such as scroll and select.

6. Test if Panache handles lifecycle events such as onCreate() and pause events correctly. 

7. Test the app on different screen sizes and resolutions. 

8. Accessibility testing to make sure app can be used by users with varying abilities. 
