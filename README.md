# address-book
#### Introduction
This is a spring boot application to facilitate persisting contacts into an address book database table as well as geting out contact information based on certain criteria
#### High level how it works
* I used a H2 database set to persist data on application restart.
* Data gets persisted in an address_books table
* There is a repository component wher I implemented two methods: one to retrieve contacts for a given addtess book and one to retrieve unique contacts accross a number of address books. The requirment was to compare two addresss books however this will work with multiple if needed.
 
#### Build
Build with Java 8 and gradle. Gradle 5 is required. You can use your own or provided gradle wrapper
#### Testing
Use curl from command prompt or postman to access the following resources:
* POST http://localhost:8080/contacts
{
"owner": "Matt",
"firstName": "John",
"lastName": "Citizen",
"phoneNo": "0424 320 665"
} This will persist an address book entry
* GET http://localhost:8080/contacts/{id}
This will retrieve a persisted contact if found
* DELETE http://localhost:8080/contacts
This will delete all persisted contacts and faciliatte starting over again and again
* GET http://localhost:8080/contacts?addressBookOwner={ownerName}
This will retrieve all contacts from a given address book identified by its owners sorted by contact name. Note the sorting is case insensitive and the last name is considered first
* GET http://localhost:8080/unique?addressBookOwners={ownerName1}, {ownerName2}, ...
This will retrieve all contacts which are unique to a single adrress book. 

#### Notes
Given the limited lime to implement this:
* I ignored any security concerns
* I did not configured any runtime environment such as docker
* I only included very basic validation such as NonEmpty conditions and I did not worry about more complex validations such as valid phone numbers formats, etc
* I tried to have as much coverage as possible eitehr with unit tests or integration tests but I am claiming is bug free
