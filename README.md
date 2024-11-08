# remote-volume-control
> Both devices must be connected to the same Local Area Network (e.g. Wifi).

## Description
#### An app to control remotely the volume of an android device.
After starting the application on the device whose sound you want to control, the application will start a web server that will run as a foreground service.
Then, a URL will be displayed (example: http://10.0.0.102:9090/?authKey=3x@mplE_S3crEt_K3y!) where you can access the page to control the sound volume by pressing the buttons or the slider.

#### Used Libraries
* **Ktor** : *A framework for building asynchronous server-side and client-side applications with ease.*

## Screenshots
---

> App - White mode - pt-BR language:

<img src="https://github.com/user-attachments/assets/9827bee8-fb60-4e30-a310-814ae952a85a" alt="white mode: start screen" width="240" height="400">
<img src="https://github.com/user-attachments/assets/6e7d341d-c05e-40a0-989f-53aeac50542c" alt="white mode: running screen" width="240" height="400">
<img src="https://github.com/user-attachments/assets/0bc858b8-5204-4662-a937-f0fc18f1e936" alt="white mode: port dialog" width="240" height="400">
<img src="https://github.com/user-attachments/assets/819ef74d-d8fc-4dcb-ba01-8a337f2ba060" alt="white mode: auth dialog" width="240" height="400">

---

> App - Dark mode - en language:

<img src="https://github.com/user-attachments/assets/f5d704d5-bb36-4cf5-bbb4-e3dcfb7f5e52" alt="dark mode: start screen" width="240" height="400">
<img src="https://github.com/user-attachments/assets/d21102b8-985d-4a0f-864f-ed8339e128e5" alt="dark mode: auto mode dialog" width="240" height="400">
<img src="https://github.com/user-attachments/assets/6120e4d2-13ab-4c17-90aa-8f9950ce7cc0" alt="dark mode: start screen auto mode on" width="240" height="400">
<img src="https://github.com/user-attachments/assets/1a18cdc8-edcd-4fec-82ce-10f77d3d745f" alt="dark mode: running screen (custom)" width="240" height="400">

---

> Web Server page:

![image](https://github.com/user-attachments/assets/b9f5dba9-6c70-4bfd-b334-23185dc47a90)

---

> Web Server page wrong auth key:

![image](https://github.com/user-attachments/assets/1777831c-35ee-4077-9adf-71d870ddca33)

---



## Important
This application is heavily inspired by Tanaka42's project, [AndroidApp-WebRemoteVolumeControl](https://github.com/tanaka42/androidapp-webremotevolumecontrol) which I am immensely grateful for his application, it was very useful to me and throughout the process, it made me learn a lot.

However, I felt that some specific features were missing. So, I decided to create a different version of the app with these features.

PS: And just like his application, this one is (and will remain) free, without ads and open source.
 

### Features
* Implemented customization of the port on which the web server will run (the default is 9090).

* Implemented basic authentication to prevent unauthorized access. You can customize the secret key value.
* Implemented automatic mode (auto mode), where the web server will be started automatically after the system's boot, without the need for manual interaction with the application.
* The web server does not stop after losing and reconnecting to the internet.
* The web server page that opens after clicking the URL, in addition to the volume up and down buttons, includes a range slider that can be used to control the volume, and also reflects the current volume of the device whenever the page is refreshed.

* The application has 4 languages:
  - english (en-US)
  - french (fr)
  - spanish (es)
  - portuguese (pt-BR)


#### Routes
In this application, the URL has a few different ways that can be used to control volume as well.
They are listed below with the URL example:

`http://10.0.0.102:9090/?authKey=3x@mplE_S3crEt_K3y!`
- web server page

`http://10.0.0.102:9090/?authKey=test`
- page with wrong auth key

`http://10.0.0.102:9090/up?authKey=3x@mplE_S3crEt_K3y!`
- increase the volume

`http://10.0.0.102:9090/down?authKey=3x@mplE_S3crEt_K3y!`
- decrease the volume

`http://10.0.0.102:9090/volume/10?authKey=3x@mplE_S3crEt_K3y!`
- increases or decreases the volume according to the value

any other URL will respond with a 404 error.

