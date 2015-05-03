# Android-App-Click-and-Clean

## 1 Team Members
* Anusha P S (IMT2012007)
* Praneeth Kumar Susarla (IMT2012043)

## 2 Aim
To promote cleanliness in the society.

## 3 Abstract
An application that can help the garbage department in maintaining cleanliness in the the city.

## 4 Process
Application is divided into two interfaces:

1. Common User Interface 

2. Department Interface

Any user (either a department member or a normal user), on the basis of their email-id's will login into their respective interfaces.

## 5 Common User Interface
If the user is not registered in the application, then he/she can register with his email-identification. Once a normal user login becomes successful, his/her GPS location is tracked by the application and it does one of the following:

1. If there are no previous complaints from that location, the user is redirected to a screen to file a new complaint for that location.

2. Otherwise it gathers complaints from that GPS location already existing in the cloud database maintained by the application then it displays:

  a. The current GPS location address

  b. The current number of complaints along with the average critical level from the same location

  c. Details of action: Provides the date if the action has been taken on the complaints coming from this location. Otherwise, it displays that the action is not yet taken on the complaints coming from this location.


The user is then provided with two options:
* REGISTER A NEW COMPLAINT: Register a new complaint on the same location conveying that proper action has not been taken by the garbage department. This is done by capturing a photo of the uncleaned area.
* CONFIRM THE COMPLAINT: To confirm that action has been taken and close the complaints coming from the location. This is done by capturing a photo of the cleaned area.

NOTE: User cannot confirm the complaint unless the action has been taken by the department on similar complaints of this location.

## 6 Department Interface
If the member is not registered in the application, then he/she can register with his email-identification to access the department interface. Otherwise, a member of the department logins into the application with his registered email-id ending with "@garbage.com". Once the login becomes successful, the application displays following three sections to the member of the department:

1. COMPLAINTS: Number of complaints along with the average critical level coming from various places (gps location addresses) waiting for the action to be taken the department. The member of the department can select the various location address complaints with the help of a check box and then use the ok button to update the taken actions on them.
NOTE: The location address of the set of complaints gets displayed by tapping it.

2. WAITING FOR CONFIRMATION: Number of complaints along with the average critical level coming from various places (gps location addresses). The action has been taken on them but are waiting for the confirmation of the action either from the department or from the normal user.

3. CLEANED: Number of complaints along with the average critical level coming from various places (gps location addresses). The action has been taken on them and the department also receives the confirmation on these complaints indicationg that the complaints have been closed.
