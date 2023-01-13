# FilePicker-Native
Android Native File Picker
A simple and light weight library for picking any type of file without taking care of Android permission. 
This library used coroutines, Rx Java & so on. you can use it in Activity & Fragment where you want..

Usage of this library :

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}


Step 2. Add the dependency

      dependencies {
	       
               implementation 'com.github.akashwebapps:FilePicker-Native:1.3

	}

Now moving to coding part here i have shown the usage of this library in Main Activity.

       lateinit var filePicker: FilePicker
       val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        filePicker = FilePicker(this, packageName)
        filePicker.setFileSelectedListener(object : FilePicker.OnFileSelectedListener {
            override fun onFileSelectSuccess(filePath: String) {

                Log.d(TAG, "onFileSelectSuccess: $filePath")

            }

            override fun onFileSelectFailure() {

                Log.d(TAG, "onFileSelectFailure: Failed")

            } })


    }



// just pass the params to that function onRequestPermissionsResult in filePicker Library..



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        filePicker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }




For selecting different file 

        // for taking image from camera
        filePicker.takePhotoFromCamera()

        // for taking video from camera
        filePicker.takeVideoFromCamera()


        // for taking image or video form gallery by selecting boolean value
        filePicker.takeFromGallery(allowPickImage = true, allowPickVideo = true)

        // for selecting all type of file
        filePicker.pickFile()

        // for selecting only pdf and image
        filePicker.pickFile(allowImage = true, allowPdf = true)


        // here you can define the file type as your need to let user choose..
        filePicker.pickFile(allowImage = false, allowPickVideo = false, allowPdf = false, allowDoc = false, allowXCL = false, allowText = false)



// That's it.. Use and Enjoy.. Thanks
🙏🙏🙏🙏 Happy Coding 🙏🙏🙏🙏

