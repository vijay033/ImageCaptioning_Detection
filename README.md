# ImageCaptioning_Detection
Tensorflow model porting in android for image captioning and object detection in a image

1. Download APK, Assest and Libs Folder from : 
https://drive.google.com/drive/folders/1ns7XIRHZhWUfUn8DvLzgaXHJXiPDVl2F?usp=sharing
2. Model for image captioning is inherited from : 
https://github.com/neural-nuts/Cam2Caption
3. Model for object detection is inherited from : 
https://github.com/martinwicke/tensorflow-tutorial/blob/master/tensorflow_inception_graph.pb
https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android
4. Add below lines in build.gradle
android{

sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
}
