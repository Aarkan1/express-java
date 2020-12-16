
#### Getting Started
```java
Express app = new Express();

app.get("/", (req, res) -> {
   res.send("Hello World");
});

app.listen(); // Will listen on port 80 which is set as default
```

## Installation

### Download
**Direct download as jar:** 
[Latest java-express-0.5.0.jar](https://github.com/Aarkan1/java-express/raw/master/releases/java-express-0.5.0.jar)

**Old version:**
[Older versions](https://github.com/Aarkan1/java-express/tree/master/releases)

### Maven
> Add repository:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

> Add dependency:
```xml
<dependency>
    <groupId>com.github.Aarkan1</groupId>
    <artifactId>java-express</artifactId>
    <version>0.5.0</version>
</dependency>
```

### Gradle
> Add this to your build.gradle
```xml
repositories {
    maven { url "https://jitpack.io/" }
}

dependencies {
    implementation 'com.github.Aarkan1:java-express:0.5.0'
}
```

## Docs:
* [Routing](#routing)
   * [Direct](#direct)
* [URL Basics](#url-basics)
   * [URL Parameter](#url-parameter)
   * [URL Parameter Listener](#url-parameter-listener)
   * [URL Querys](#url-querys)
   * [Cookies](#cookies)
   * [Server Side Events](#server-side-events)
   * [Form Data](#form-data)
   * [FileItem Object](#fileitem-object)
* [HTTP Relevant classes](#http-relevant-classes)
   * [Response Object](#response-object)
   * [Request Object](#request-object)
* [Embedded database - Collections](#embedded-database---collections)
* [Middleware](#middleware)
   * [Create own middleware](#create-own-middleware)
* [Using global variables](#global-variables)
* [Examples](#examples)
   * Very simple static-website
   * CRUD with embedded Collection database
   * File upload
   * Send cookies 
   * File download
* [Router code splitting](#router-code-splitting)
   * [With Router](#with-router)
   * [DynExpress](#dynexpress)
* [License](#license)

## Routing
### Direct
You can add routes (And middlewares) directly to the Express object to handle requests:
```java
Express app = new Express();

// Sample for home routes
app.get("/", (req, res) -> res.send("Hello index!"));
app.get("/home", (req, res) -> res.send("Homepage"));
app.get("/about", (req, res) -> res.send("About"));

// Sample for user
app.get("/user/login", (req, res) -> res.send("Please login!"));
app.get("/user/register", (req, res) -> res.send("Join now!"));

app.listen();
```
Directly it also supports methods like `POST` `PATCH` `DELETE` and `PUT`, others need to be created manually:
```java
Express app = new Express();

// Basic methods
app.get("/user", (req, res) -> res.send("Get an user!"));
app.patch("/user", (req, res) -> res.send("Modify an user!"));
app.delete("/user", (req, res) -> res.send("Delete an user!"));
app.put("/user", (req, res) -> res.send("Add an user!"));
app.post("/user", (req, res) -> res.send("Create an user!"));

// Example for the CONNECT method
app.on("/user", "CONNECT", (req, res) -> res.send("Connect!"));

app.listen();
```

## URL Basics
With the express object you can create handler for all [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts. Some examples:
```java
app.get("/home", (req, res) -> {
	// Will match every request which uses the 'GET' method and matches the '/home' path
});

app.post("/login", (req, res) -> {
	// Will match every request which uses the 'POST' method and matches the /login' path
});
```

<details>
    <summary>Show documentation</summary>

### URL Parameter
Sometimes you want to create dynamic URL where some parts of the URL's are not static.
With the `:` operator you can create variables in the URL which will be saved later in a HashMap.

Example request: `GET`  `/posts/john/all`:
```java
app.get("/posts/:user/:description", (req, res) -> {
   String user = req.getParam("user"); // Contains 'john'
   String description = req.getParam("description"); // Contains 'all'
   res.send("User: " + user + ", description: " + description); // Send: "User: john, description: all"
});
```

#### URL Parameter Listener
You can also add an event listener when the user called an route which contains an certain parameter.

```java
app.get("/posts/:user/:id", (req, res) -> {
  // Code
});
```
For example, if we want to check every `id` before the associated get post etc. handler will be fired, we can use the `app.onParam([PARAM])` function:
```java
app.onParam("id", (req, res) -> {
  // Do something with the id parameter, eg. check if it's valid.
});
```
Now, this function will be called every time when an context is requested which contains the `id` parameter placeholder.

### URL Querys
If you make an request which contains querys, you can access the querys over `req.getQuery(NAME)`.

Example request: `GET`  `/posts?page=12&from=john`:
```java
app.get("/posts", (req, res) -> {
   String page = req.getQuery("page"); // Contains '12'
   String from = req.getQuery("from"); // Contains 'John'
   res.send("Page: " + page + ", from: " + from); // Send: "Page: 12, from: John"
});
```

</details>

## Cookies
With `req.getCookie(NAME)` you can get an cookie by his name, and with `res.setCookie(NAME, VALUE)` you can easily set an cookie.

<details>
    <summary>Show examples</summary>

Example request: `GET`  `/setcookie`:
```java
app.get("/setcookie", (req, res) -> {
   Cookie cookie = new Cookie("username", "john");
   res.setCookie(cookie);
   res.send("Cookie has been set!");
});
```

Example request: `GET`  `/showcookie`:
```java
app.get("/showcookie", (req, res) -> {
   Cookie cookie = req.getCookie("username");
   String username = cookie.getValue();
   res.send("The username is: " + username); // Prints "The username is: john"
});
```

</details>

## Server Side Events
With `app.sse()`

<details>
    <summary>Show documentation</summary>

`res.send(EVENT, DATA)` takes the event as a String, and data as a String or an Object. The object will be converted to json before sending to clients. 
If you want use SSE (Server Side Events) you'll have to create an endpoint for the client to connect to, for example in JavaScript via `new EventSource('/sse-endpoint')`. 
Closing the SSE with `res.reconnectSSE()` will force the client to reconnect. If `res.reconnectSSE()` doesn't get called the SSE will stay connected and `res.send(EVENT, DATA)` can be used multiple times before closing.

Example Java:
```java
app.sse("/sse-endpoint", (req, res) -> {
   for(int i = 0; i < 20; i++) {

      res.send("testEvent", "Test nr: " + i);

      try {
            Thread.sleep(1000);
      } catch (InterruptedException e) {
            e.printStackTrace();
      }
   }

   res.reconnectSSE(); // this will close the event-stream, forcing the client to reconnect
});
```

Example JavaScript:
```js
let testSource = new EventSource('/sse-endpoint');

// the listener will trigger every 1 second with the above example,
// then it will reconnect and start over from 0
testSource.addEventListener('testEvent', (event) => {
   console.log(event.data);
   /*
      Prints:
      Test nr: 0
      Test nr: 1
      Test nr: 2
      etc...
   */
});
```

</details>

### Form data
With `req.getFormData(NAME)` you receive a list of FileItems from the posted FormData.
`req.getFormData()` without a param will return a Map, where key is the field name and value the list of FileItems.

<details>
    <summary>Show available methods</summary>

Example JavaScript:
```js
let files = document.querySelector('input[type=file]').files;
let formData = new FormData();

for(let file of files) {
   formData.append('files', file, file.name);
}
   
formData.append('greeting', 'Hello, awesome server!');

fetch('/api/file-upload', {
   method: 'POST',
   body: formData
});
```

Form data gets stored in lists. If a file is appended we get the byte[] array from the fileItem.get(). You can use this byte[] array to save the file to an uploads-folder. 
```java
app.post("/api/file-upload", (req, res) -> {
  List<FileItem> files = req.getFormData("files");
  String greeting = req.getFormData("greeting").get(0).getString();

  String filename = files.get(0).getName();
  byte[] profilePic = files.get(0).get();
  // Process data, save files to disk

  // Prints "Greeting: Hello, awesome server!, Filename: profile-picture.png"
  res.send("Greeting: " + greeting + ", Filename: " + filename);
});
```

### FileItem Object
From `req.getFormData(NAME)` you get a list of FileItems. 

<details>
    <summary>Show fileItem methods</summary>

```java
fileItem.get();                  // Returns the file byte[] array
fileItem.getName();              // Returns the file name
fileItem.getString();            // Returns the form data value as String, default charset = UTF_8
fileItem.getString(String encoding); // Returns the form data value as String with provided encoding
fileItem.getContentType();       // Returns the content type
fileItem.getFieldName();         // Returns the form data field name
fileItem.getInputStream();       // Returns the file inputstream
fileItem.getOutputStream();      // Returns the file outputstream
fileItem.getSize();              // Returns the form data size
```

</details>

With `req.getFormQuery(NAME)` you receive the values from the input elements of an HTML-Form.
Example HTML-Form:
```html
<form action="http://localhost/register" method="post">
   <input description="text" name="email" placeholder="Your E-Mail">
   <input description="text" name="username" placeholder="Your username">
   <input description="submit">
</form>
```
**Attention: Currently, File-inputs don't work in forms, if there is an File-input the data won't get parsed!**
**To upload files you need to add them to FormData in the frontend!**
Now description, for the example below, `john` in username and `john@gmail.com` in the email field.
Java code to handle the post request and access the form elements:
```java
app.post("/register", (req, res) -> {
  String email = req.getFormQuery("email");
  String username = req.getFormQuery("username");
  // Process data

  // Prints "E-Mail: john@gmail.com, Username: john"
  res.send("E-Mail: " + email + ", Username: " + username);
});
```

</details>

## HTTP Relevant classes
### Express
This class represents the entire HTTP-Server. 

<details>
    <summary>Show available methods</summary>

```java
app.get(String context, HttpRequest handler);                   // Add an GET request handler
app.post(String context, HttpRequest handler);                  // Add an POST request handler
app.patch(String context, HttpRequest handler);                 // Add an PATCH request handler
app.put(String context, HttpRequest handler);                   // Add an PUT request handler
app.delete(String context, HttpRequest handler);                // Add an DELETE request handler
app.all(HttpRequest handler);                                   // Add an handler for all methods and contexts
app.all(String context, HttpRequest handler);                   // Add an handler for all methods but for an specific context
app.all(String context, String method, HttpRequest handler);    // Add an handler for an specific method and context
app.use(String context, String method, HttpRequest handler);    // Add an middleware for an specific method and context
app.use(HttpRequest handler);                                   // Add an middleware for all methods but for an specific context
app.use(String context, HttpRequest handler);                   // Add an middleware for all methods and contexts
app.use(String context, ExpressRouter router);                  // Add an router for an specific root context
app.use(ExpressRouter router);                                  // Add an router for the root context (/)
app.onParam(String name, HttpRequest handler);                  // Add an listener for an specific url parameter
app.sse(String context, HttpRequest handler);                   // Add an handler for Server Side Events
app.getParameterListener();                                     // Returns all parameterlistener
app.get(String key);                                            // Get an environment variable
app.set(String key, String val);                                // Set an environment variable
app.isSecure();                                                 // Check if the server uses HTTPS
app.setExecutor(Executor executor);                             // Set an executor service for the request
app.enableCollections();                                        // Enables the embedded document database
app.enableCollections(CollectionOptions options);               // Enables the database with options
app.listen();                                                   // Start the async server on port 80
app.listen(ExpressListener onstart);                            // Start the async server on port 80, call the listener after starting
app.listen(int port);                                           // Start the async server on an specific port
app.listen(ExpressListener onstart, int port);                  // Start the async server on an specific port call the listener after starting
app.stop();                                                     // Stop the server and all middleware worker
```

</details>

### Response Object
With the `Response` object, you have several possibility like setting cookies, send an file and more.
(We assume that `res` is the `Response` object)

<details>
    <summary>Show response methods</summary>

```java
res.getContentType();                  // Returns the current content type
res.setContentType(MediaType type);    // Set the content type with enum help
res.setContentType(String type);       // Set the content type
res.getContentLength();                // Returns the content length
res.getRaw();                          // Returns the raw HttpExchange object
res.isClosed();                        // Check if the response is already closed
res.getHeader(String key);             // Get the value from an header field via key
res.setHeader(String key, String val); // Add an specific response header
res.sendAttachment(Path file)          // Sends a file as attachment
res.send(String str);                  // Send a string as response
res.send(Path path);                   // Send a file as response
res.send(byte[] bytes)                 // Send bytes as response
res.send();                            // Send empty response
res.send(String event, String data);   // Send a server side event message to target listener
res.send(String event, Object data);   // Send a server side event object as json to target listener
res.reconnectSSE();                    // Close the ongoing server side event session (note: client will automatically reconnect after 3 seconds)
res.redirect(String location);         // Redirect the request to another url
res.setCookie(Cookie cookie);          // Add an cookie to the response
res.sendStatus(Status status);         // Set the response status and send an empty response
res.getStatus();                       // Returns the current status
res.setStatus(Status status);          // Set the repose status
res.streamFrom(long contentLength, InputStream is, MediaType mediaType) // Send a inputstream with known length and type
res.json(Object object);               // Send object as JSON response
```
The response object calls are comments because **you can only call the .send(xy) once each request!**

</details>

### Request Object
With the `Request` object you have access to several request stuff (We assume that `req` is the `Request` object):

<details>
    <summary>Show request methods</summary>

```java
req.getAddress();                 // Returns the INET-Adress from the client
req.getMethod();                  // Returns the request method
req.getPath();                    // Returns the request path
req.getContext();                 // Returns the corresponding context
req.getQuery(String name);        // Returns the query value by name
req.getHost();                    // Returns the request host
req.getContentLength();           // Returns the content length
req.getContentType();             // Returns the content type
req.getMiddlewareContent(String name); // Returns the content from an middleware by name
req.getFormData();                // Returns all form datas
req.getFormData(String name);     // Returns all form data from the form field by name
req.getFormQuerys();              // Returns all form querys
req.getParams();                  // Returns all params
req.getQuerys();                  // Returns all querys
req.getFormQuery(String name);    // Returns the form value by name
req.getHeader(String key);        // Returns the value from an header field by name
req.getParam(String key);         // Returns the url parameter by name
req.getApp();                     // Returns the related express app
req.getCookie(String name);       // Returns an cookie by his name
req.getCookies();                 // Returns all cookies
req.getIp();                      // Returns the client IP-Address
req.getUserAgent();               // Returns the client user agent
req.getURI();                     // Returns the request URI
req.isFresh();                    // Returns true if the connection is fresh, false otherwise (see code inline-doc)
req.isStale();                    // Returns the opposite of req.fresh;
req.isSecure();                   // Returns true when the connection is over HTTPS, false otherwise
req.isXHR();                      // Returns true if the 'X-Requested-With' header field is 'XMLHttpRequest'
req.getProtocol();                // Returns the connection protocol
req.getAuthorization();           // Returns the request authorization
req.hasAuthorization();           // Check if the request has an authorization
req.pipe(OutputStream stream, int buffersize); // Pipe the request body to an outputstream
req.pipe(Path path, int buffersize);           // Pipe the request body to an file
req.getBody();                    // Returns the request as a Map<String, String>
req.getBodyStream();              // Returns the request inputstream
req.getBody(Class klass);         // Returns the request object as target class
```

</details>

## Embedded database - Collections
Collection is a server-less embedded database ideal for small web applications. It's based on the open source project [Nitrite Database](https://www.dizitart.org/nitrite-database.html).

This database is a built in feature in Java Express, and available to use with a simple `app.enableCollections()`.

**It features:**
- Embedded key-value object store
- Single file store
- Very fast and lightweight MongoDB like API
- Indexing
- Full text search capability
- Observable store

*Requires Java Express version 0.5.0 and above!*

See [Examples](#examples) for an example of *CRUD with embedded Collection database*.

<details>
    <summary>Show documentation</summary>

## Table of content
- [Getting started](#getting-started)
- [CollectionOptions](#collectionoptions)
    - [Important note!](#important-note)
- [Browse Collections](#browse-collections)
- [Annotations](#annotations)
- [Collection methods](#collection-methods)
    - [Filters](#filters)
    - [FindOptions](#findoptions)
- [Examples](#examples)

## Getting started
The Express app has an embedded nosql database, ready to be used if you enable it by adding `app.enableCollections()` right after app is instantiated. 
This will create a database-file in your project. Easy to deploy or share.
When collections are enabled you can use the static `collection()`-method to manipulate the database. 
**collection()** takes either a String with the classname, case sensitive, or the Class itself. 

```java
import static express.database.Database.collection;

Express app = new Express();
// creates a database-file in /db-folder called 'embedded.db'
app.enableCollections(); 
// creates the file at target path
app.enableCollections(String dbPath); 


User john = new User("John").
// generates an UUID
collection("User").save(john); 

User jane = collection("User").findById("xxxxxxxx-xxxx-4xxx-8xxx-xxxxxxxxxxxx");

jane.setAge(30);
// updates model with same UUID
collection("User").save(jane); 

// delete Jane
collection("User").deleteById("xxxxxxxx-xxxx-4xxx-8xxx-xxxxxxxxxxxx"); 

List<User> users = collection("User").find();
List<User> users = collection(User.class).find();

List<User> usersNamedJohn = collection("User").find(eq("name", "John"));
```

Watch a collection on changes
```java
// watchData has 2 fields. 
// getEvent() is the event triggered - 'insert', 'update' or 'delete'
// getData() is a list with effected models
collection("User").watch(watchData -> {
    List<User> effectedUsers = watchData.getData();

    switch(watchData.getEvent()) {
        case "insert": // on created model
        break;

        case "update": // on updated model
        break;

        case "delete": // on deleted model
        break;
    }
});
```

### CollectionOptions
CollectionOptions can be passed when enabling collections to set certain options.
Options available are:
- *CollectionOptions.ENABLE_SSE_WATCHER* - Enables Server Side Events listener on collection changes
- *CollectionOptions.DISABLE_BROWSER* - Disables collection browser (good when deploying)

You can pass one or multiple options when enabling collections:
```java
Express app = new Express();
app.enableCollections(CollectionOptions.ENABLE_SSE_WATCHER, CollectionOptions.DISABLE_BROWSER);
```

**ENABLE_SSE_WATCHER**

This starts an event stream endpoint in the database that will send a Server Side Event when a change happens.

To listen to these events on the client you have to create a connection to `'/watch-collections'` with an `EventSource`.

```js
let colls = new EventSource('/watch-collections')
```

With the eventSource you can add listeners to each model in the collection.

```js
// listen to changes to the 'BlogPost' collection 
colls.addEventListener('BlogPost', (messageEvent) => {
    // handle event
});

// listen to changes to the 'Message' collection 
colls.addEventListener('Message', (messageEvent) => {
    // handle event
});
```

#### Example

Java:
```java
Express app = new Express();
app.enableCollections(CollectionOptions.ENABLE_SSE_WATCHER);
```

JavaScript:
```js
let colls = new EventSource('/watch-collections');

colls.addEventListener('BlogPost', (messageEvent) => {
    const { event, data } = JSON.parse(messageEvent.data);
    console.log("BlogPost event:", event, data);

    switch(event) {
        case 'insert':
            // add new post to list
            posts.push(data[0]);
        break;
        case 'update':
            // do something on update
        break;
        case 'delete':
            // remove deleted post from list
            posts = posts.filter(post => post.id !== data[0].id);
        break;
    }

    // update 
    renderPosts();
});

colls.addEventListener('Message', (messageEvent) => {
    const { event, data } = JSON.parse(messageEvent.data);
    console.log('Message event:', event, data);
});
```

**DISABLE_BROWSER**

This will simple disable the collection browser. This might be a good idea to save CPU and RAM when deploying. 

```java
Express app = new Express();
app.enableCollections(CollectionOptions.DISABLE_BROWSER);
```


### Important note!
After a model is saved to the collection, the class with **@Model** annotation **CANNOT** be moved to another package or renamed. This will corrupt the database-file, and will have to be removed. 
Keep backups!

Changing the name of a field will not corrupt the database, but will remove the value from all models.


## Browse Collections
When collections is enabled the Collection Browser by default gets accessible in your webb browser on url `http://localhost:9595`.
This URL is protected by CORS. 

This can be disabled with `CollectionOptions.DISABLE_BROWSER` option.

Currently the Collection Browser only supports:
* Viewing models in a collection
* Deleting a model from a collection

*Features coming in near future:*
- *Be able to update a field in a model*
- *Be able to import data with .json-file*
- *Pagination on large collections*

## Annotations
For the collections to work the following two annotations must be present in at least one class.

### @Model Annotation
Marks a class to be used with a collection. Is required if an object is going to be saved to the collection.

### @Id Annotation
Each object in a Collection must be uniquely identified by a field marked with **@Id** annotation. The collection maintains an unique index on that field to identify the objects.
If no id is manually set, the Collection will generate an UUID to that field when inserted or saved. 

```java
import express.database.Model;
import org.dizitart.no2.objects.Id;

@Model
public class MyType {

    @Id
    private String id;
    private String name;
}
```

The collection provides a set of annotations for model objects while using it in a collection. The annotations are to let the collection know about various information about the **model** while constructing it. It also helps to reduce some boilerplate code.

**@Index** is required to do **text()** or **regex()** filtering on a field. It can only be used within a **@Indices** annotation.
**Index types are:**
- IndexType.Unique - used with unique fields
- IndexType.NonUnique - used with single value duplicate fields
- IndexType.FullText - used with multiple word fields, NonUnique

Example
```java
// Employee class
@Indices({
        @Index(value = "joinDate", type = IndexType.NonUnique),
        @Index(value = "name", type = IndexType.Unique)
})
@Model
public class Employee {
    @Id
    private String id;
    private Date joinDate;
    private String name;
    private String address;

    // ... public getters and setters
}
```

## Collection methods

To use the collection you need to add which model to query for in the collection parameter, ex `collection("User")` will only query for Users. 

**Table 1. Collection methods**

| Operation | Method | Description |
| --- | --- | --- |
| Get all models | find(Filter, SortOptions) | Returns a list with objects. If no filter is used find() will return ALL models. |
| Get one model | findOne(Filter) | Returns first found model. |
| Get model with id | findById(String) | Returns the object with mathing id. |
| Create new model | insert(Object) | Creates a new model in the collection. Generates an UUID if no id is present. Can insert an array of models. |
| Create or Update a model | save(Object) | Creates a new model in the collection if no id is present. If theres an id save() will update the existing model in the collection. Can save an array of models. |
| Update models | update(Filter, Object) | Update all models matching the filter. |
| Update a model with id | updateById(String) | Updates the model with matching id. |
| Delete models | delete(Filter) | Deletes all models matching the filter. |
| Delete a model with id | deleteById(String) | Deletes the model with matching id. |
| Watch a collection | watch(lambda) | Register a watcher that triggers on changes in the collection. |


### Filters

Filters are the selectors in the collection’s find operation. It matches models in the collection depending on the criteria provided and returns a list of objects.

Make sure you import the static method **ObjectFilters**.

```java
import static org.dizitart.no2.objects.filters.ObjectFilters.*;
```

**Table 2. Comparison Filter**

| Filter | Method | Description |
| --- | --- | --- |
| Equals | eq(String, Object) | Matches values that are equal to a specified value. |
| Greater | gt(String, Object) | Matches values that are greater than a specified value. |
| GreaterEquals | gte(String, Object) | Matches values that are greater than or equal to a specified value. |
| Lesser | lt(String, Object) | Matches values that are less than a specified value. |
| LesserEquals | lte(String, Object) | Matches values that are less than or equal to a specified value. |
| In | in(String, Object[]) | Matches any of the values specified in an array. |
| NotIn | notIn(String, Object[]) | Matches none of the values specified in an array. |

**Table 3. Logical Filters**

| Filter | Method | Description |
| --- | --- | --- |
| Not | not(Filter) | Inverts the effect of a filter and returns results that do not match the filter. |
| Or | or(Filter[]) | Joins filters with a logical OR returns all ids of the models that match the conditions of either filter. |
| And | and(Filter[]) | Joins filters with a logical AND returns all ids of the models that match the conditions of both filters. |

**Table 4. Array Filter**

| Filter | Method | Description |
| --- | --- | --- |
| Element Match | elemMatch(String, Filter) | Matches models that contain an array field with at least one element that matches the specified filter. |

**Table 5. Text Filters**
*Note*: For these filters to work the field must be indexed. See [Annotations](#annotations)

| Filter | Method | Description |
| --- | --- | --- |
| Text | text(String, String) | Performs full-text search. |
| Regex | regex(String, String) | Selects models where values match a specified regular expression. |

### FindOptions

A FindOptions is used to specify search options. It provides pagination as well as sorting mechanism.

```java
import static org.dizitart.no2.FindOptions.*;
```

Example
```java
// sorts all models by age in ascending order then take first 10 models and return as a List
List<User> users = collection("User").find(sort("age", SortOrder.Ascending).thenLimit(0, 10));
```
```java
// sorts the models by age in ascending order
List<User> users = collection("User").find(sort("age", SortOrder.Ascending));
```
```java
// sorts the models by name in ascending order with custom collator
List<User> users = collection("User").find(sort("name", SortOrder.Ascending, Collator.getInstance(Locale.FRANCE)));
```
```java
// fetch 10 models starting from offset = 2
List<User> users = collection("User").find(limit(2, 10));
```

## Filter examples

**and()**
```java
// matches all models where 'age' field has value as 30 and
// 'name' field has value as John Doe
collection("User").find(and(eq("age", 30), eq("name", "John Doe")));
```

**or()**
```java
// matches all models where 'age' field has value as 30 or
// 'name' field has value as John Doe
collection("User").find(or(eq("age", 30), eq("name", "John Doe")));
```

**not()**
```java
// matches all models where 'age' field has value not equals to 30
collection("User").find(not(eq("age", 30)));
```

**eq()**
```java
// matches all models where 'age' field has value as 30
collection("User").find(eq("age", 30));
```

**gt()**
```java
// matches all models where 'age' field has value greater than 30
collection("User").find(gt("age", 30));
```

**gte()**
```java
// matches all models where 'age' field has value greater than or equal to 30
collection("User").find(gte("age", 30));
```

**lt()**
```java
// matches all models where 'age' field has value less than 30
collection("User").find(lt("age", 30));
```

**lte()**
```java
// matches all models where 'age' field has value lesser than or equal to 30
collection("User").find(lte("age", 30));
```

**in()**
```java
// matches all models where 'age' field has value in [20, 30, 40]
collection("User").find(in("age", 20, 30, 40));
```

**notIn()**
```java
// matches all models where 'age' field does not have value in [20, 30, 40]
collection("User").find(notIn("age", 20, 30, 40));
```

**elemMatch()**
```java
// matches all models which has an array field - 'color' and the array
// contains a value - 'red'.
collection("User").find(elemMatch("color", eq("$", "red"));
```

**text()**
```java
// matches all models where 'address' field has a word 'roads'.
collection("User").find(text("address", "roads"));

// matches all models where 'address' field has word that starts with '11A'.
collection("User").find(text("address", "11a*"));

// matches all models where 'address' field has a word that ends with 'Road'.
collection("User").find(text("address", "*road"));

// matches all models where 'address' field has a word that contains a text 'oa'.
collection("User").find(text("address", "*oa*"));

// matches all models where 'address' field has words like '11a' and 'road'.
collection("User").find(text("address", "11a road"));

// matches all models where 'address' field has word 'road' and another word that start with '11a'.
collection("User").find(text("address", "11a* road"));
```

**regex()**
```java
// matches all models where 'name' value starts with 'jim' or 'joe'.
collection("User").find(regex("name", "^(jim|joe).*"));
```

</details>

# Middleware
Middleware are one of the most important features of JavaExpress, with middleware you can handle a request before it reaches any other request handler. 

To create an own middleware you have serveral interfaces:
* `HttpRequest`  - Is **required** to handle an request.
* `ExpressFilter` - Is **required** to put data on the request listener.
* `ExpressFilterTask` - Can be used for middleware which needs an background thread.

Middlewares work, for you, exact same as request handler.
For example an middleware for all [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts:

```java
// Global context, matches every request.
app.use((req, res) -> {
  // Handle data
});
```
You can also filter by [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts:
```java
// Global context, you can also pass an context if you want
app.use("/home", "POST", (req, res) -> {
  // Handle request by context '/home' and method 'POST'
});
```
In addition to that yo can use `*` which stands for every **context** or **request-method**:
```java
// Global context, you can also pass an context if you want
app.use("/home", "*", (req, res) -> {
  // Handle request which matches the context '/home' and all methods.
});
```
## Create own middleware

Now we take a look how we can create own custom middlewares. 

<details>
    <summary>Show documentation</summary>

Here we create an simple PortParser which parse / extract the port-number for us. We only used `HttpRequest` and `ExpressFilter` because we don't need any background thread.
```java
public class PortMiddleware implements HttpRequest, ExpressFilter {

   /**
    * From interface HttpRequest, to handle the request.
    */
   @Override
   public void handle(Request req, Response res) {
      
      // Get the port
      int port = req.getURI().getPort();
      
      // Add the port to the request middleware map
      req.addMiddlewareContent(this, port);

      /**
       * After that you can use this middleware by call:
       *   app.use(new PortMiddleware());
       *   
       * Than you can get the port with:
       *   int port = (Integer) app.getMiddlewareContent("PortParser");
       */
   }

   /**
    * Defines the middleware.
    *
    * @return The middleware name.
    */
   @Override
   public String getName() {
      return "PortParser";
   }
}
```

No we can, as we learned above, include it with:
```java
// Global context, you can also pass an context if you want
app.use(new PortMiddleware());
```

And use it:
```java
app.get("/port-test", (req, res) -> {
  
  // Get the content from the PortParser which we create above
  int port = (Integer) req.getMiddlewareContent("PortParser");
   
  // Return it to the client:
  res.send("Port: " + port);
});
```

</details>

## Existing Middlewares
There are already some basic middlewares included, you can access these via static methods provided from `Middleware`.

<details>
    <summary>Show available middlewares</summary>

#### CORS
To realize a cors api you can use the cors middleware.
```java
app.use(Middleware.cors());
```
You can use CorsOptions to specify origin, methods and more:
```java
CorsOptions corsOptions = new CorsOptions();
corsOptions.setOrigin("https://mypage.com");
corsOptions.setAllowCredentials(true);
corsOptions.setHeaders(new String[]{"GET", "POST"});
corsOptions.setFilter(req -> // Custom validation if cors should be applied);
        
app.use(Middleware.cors());
```

#### Provide static Files
If you want to allocate some files, like librarys, css, images etc. you can use the [static](https://github.com/Aarkan1/java-express/blob/master/src/express/middleware/Middleware.java) middleware. But you can also provide other files like mp4 etc.
Example:
```java
 app.use(Middleware.statics("examplepath\\myfiles"));
```
Now you can access every files in the `test_statics` over the root adress `\`. It's also possible to set a configuration for the FileProvider:
```java
FileProviderOptionsoptions = new FileProviderOptions();
options.setExtensions("html", "css", "js"); // By default, all are allowed.

/*
 * Activate the fallbacksearch.
 * E.g. if an request to <code>/js/code.js</code> was made but the
 * requested resource cannot be found. It will be looked for an file called <code>code</code>
 * and return it.
 *
 *  Default is false
 */
options.setFallBackSearching(true);
options.setHandler((req, res) -> {...});    // Can be used to handle the request before the file will be returned.
options.setLastModified(true);              // Send the Last-Modified header, by default true.
options.setMaxAge(10000);                   // Send the Cache-Control header, by default 0.
options.setDotFiles(DotFiles.DENY);         // Deny access to dot-files. Default is IGNORE.
app.use(Middleware.statics("examplepath\\myfiles", new FileProviderOptions())); // Using with StaticOptions
```
#### Cookie Session
There is also an simple cookie-session implementation:
```java
// You should use an meaningless cookie name for several security reasons, here f3v4.
// Also you can specify the maximum age of the cookie from the creation date and the file types which are actually allowed.
app.use(Middleware.cookieSession("f3v4", 9000));
```
To use a session cookie we need to get the data from the middleware which is actually an `SessionCookie`:
```java
 // Cookie session example
app.get("/session", (req, res) -> {

   /**
   * CookieSession named his data "Session Cookie" which is
   * an SessionCookie so we can Cast it.
   */
   SessionCookie sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");
   int count;
   
Check if the data is null, we want to implement an simple counter
   if (sessionCookie.getData() == null) {
   
      // Set the default data to 1 (first request with this session cookie)
      count = (Integer) sessionCookie.setData(1);
   
   } else {
      // Now we know that the cookie has an integer as data property, increase it
      count = (Integer) sessionCookie.setData((Integer) sessionCookie.getData() + 1);
   }

Send an info message
   res.send("You take use of your session cookie " + count + " times.");
});
```

</details>

### Global Variables
Java Express also supports to save and read global variables over the Express instance.
Endpoint calls is handled on separate threads, and accessing variables outside the handler might trigger race conditions. 
With `app` you get a secure holder for global variables, that is thread-safe.

Example:
```java
app.set("my-data", "Hello World");
app.get("my-data"); // Returns "Hello World"
```

## Examples
- Very simple static-website
- CRUD with embedded Collection database
- File upload
- Send cookies 
- File download

<details>
    <summary>Show examples</summary>

#### Very simple static-website
```java
// Create instance
Express app = new Express();

// will serve both the html/css/js files and the uploads folder in target directory
try {
   app.use(Middleware.statics(Paths.get("src/www").toString()));
} catch (IOException e) {
   e.printStackTrace();
}

app.listen(4000); // start server on port 4000
```
#### CRUD with embedded Collection database
```java
// Create instance
Express app = new Express();
// Enable collections before handlers
app.enableCollections();

// Get all articles
app.get("/articles", (req, res) -> {
   List<Article> articles = collection("Article").find();
   res.json(articles);
});

// Get article with id param
app.get("/articles/:id", (req, res) -> {
   String articleId = req.getParam("id");
   Article article = collection("Article").findById(articleId);
   res.json(article);
});

// Create new article or update existing if existing 'id' is present,
// and return same article with generated 'id' if it wasn't present
app.post("/articles", (req, res) -> {
   Article article = req.getBody(Article.class);
   Article articleWithGeneratedId = collection("Article").save(article);
   res.json(articleWithGeneratedId);
});

// Delete article with id param
app.delete("/articles/:id", (req, res) -> {
   String articleId = req.getParam("id");
   int affectCount = collection("Article").deleteById(articleId);

   if(affectCount > 0) {
      res.send("Delete ok");
   } else {
      res.send("Could not delete article");
   }
});

app.listen(4000); // start server on port 4000


// Class tagged as a Model to be saved in the collection
import express.database.Model;
import org.dizitart.no2.objects.Id;

@Model // required
class Article {
   @Id // required
   private String id;
   private String title;
   private String content;

   // getters, setters, etc..
}
```

#### File upload
Server
```java
// Create instance
Express app = new Express();

// Define endpoint to send formData with files
app.post("/api/file-upload", (req, res) -> {
   // Define list that will contain upload urls to send back to client
   List<String> uploadNames = new ArrayList<>();
   // extract the file from the FormData
   List<FileItem> files = req.getFormData("files");

   // loop files
   for (FileItem file : files) {
      // get filename and concat with upload folder name
      String filename = "/uploads/" + file.getName();
      // add upload filename to list
      uploadNames.add(filename);

      // open an ObjectOutputStream with the path to the uploads folder in target directory,
      // in this case "src/www/uploads/", typically in the folder you serve as static
      try (var os = new FileOutputStream(Paths.get("src/www" + filename).toString())) {
         // get the required byte[] array to save to a file
         // with file.get()
         os.write(file.get());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   // return uploaded filenames to client
   res.json(uploadNames);
});
```

Client (JavaScript)
```js
async function uploadFiles(e) {
    e.preventDefault();

    // upload files with FormData
    let files = document.querySelector('input[type=file]').files;

    if(files.length) {
        let formData = new FormData();
        
        // add files to formData
        for(let file of files) {
            formData.append('files', file, file.name);
        }
        
        // upload selected files to server
        let uploadResult = await fetch('/api/file-upload', {
            method: 'POST',
            body: formData
        });
        
        // get the uploaded file urls from response
        let uploadNames = await uploadResult.json();
    }
}
```

#### Send cookies 
```java
// Create instance
Express app = new Express();

// Define route
app.get("/give-me-cookies", (req, res) -> {

   // Set an cookie (you can call setCookie how often you want)
   res.setCookie(new Cookie("my-cookie", "Hello World!"));
   
   // Send text
   res.send("Your cookie has been set!");
});

app.listen(4000); // start server on port 4000
```

#### File download
```java
// Create instance
Express app = new Express();

// Your file
Path downloadFile = Paths.get("my-big-file");

// Create get-route where the file can be downloaded
app.get("/download-me", (req, res) -> res.sendAttachment(downloadFile));

app.listen(4000); // start server on port 4000
```

</details>

## Router code splitting
### With Router
It's better to split your code, right? With the `ExpressRouter` you can create routes and add it later to the `Express` object.

<details>
    <summary>Show example</summary>

```java
Express app = new Express() {{

  // Define root greeting                                         // url
  get("/", (req, res) -> res.send("Hello World!"));               // '/'

  // Define home routes
  use("/home", new ExpressRouter() {{
    get("/about", (req, res) -> res.send("About page"));          // '/home/about'
    get("/impressum", (req, res) -> res.send("Impressum page"));  // '/home/impressum'
    get("/sponsors", (req, res) -> res.send("Sponsors page"));    // '/home/sponsors'
  }});

  // Define root routes
  use("/", new ExpressRouter() {{
    get("/login", (req, res) -> res.send("Login page"));          // '/login'
    get("/register", (req, res) -> res.send("Register page"));    // '/register'
    get("/contact", (req, res) -> res.send("Contact page"));      // '/contact'
  }});

  // Start server
  listen();
}};
```

</details>

### DynExpress
Express allows the attaching of request-handler to instance methods via the DynExpress annotation.

<details>
    <summary>Show example</summary>

```java
// Your main class
import express.Express;

public class Main {
    public static void main(String[] args) {
        Express app = new Express();
        app.bind(new Bindings()); // See class below
        app.listen();
    }
}

// Your class with request handlers
import express.DynExpress;
import express.http.RequestMethod;
import express.http.request.Request;
import express.http.response.Response;
public class Bindings {

    @DynExpress() // Default is context="/" and method=RequestMethod.GET
    public void getIndex(Request req, Response res) {
        res.send("Hello World!");
    }

    @DynExpress(context = "/about") // Only context is defined, method=RequestMethod.GET is used as method
    public void getAbout(Request req, Response res) {
        res.send("About page");
    }

    @DynExpress(context = "/impressum", method = RequestMethod.PATCH) // Both defined
    public void getImpressum(Request req, Response res) {
        res.send("Impressum page was patched");
    }

    @DynExpress(method = RequestMethod.POST) // Only the method is defined, "/" is used as context
    public void postIndex(Request req, Response res) {
        res.send("POST to index");
    }
}
```

</details>

### License
This project is licensed under the MIT License - see the [LICENSE](https://github.com/Aarkan1/java-express/blob/master/LICENSE) file for details.
