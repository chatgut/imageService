## Spring Boot Application using GraalVM - Image Microservice

This is a sample Spring Boot application that uses GraalVM to build a native image. The microservice handles images and can receive and upload them. If images have been uploaded they can also be resized to a smaller size to fit a thumbnail.

Runs on port 8001.

## Getting Started

Start a mySQL docker container;

docker run --name imageDB -e MYSQL_ROOT_PASSWORD=secret-pw -e 'MYSQL_ROOT_HOST=%' -e MYSQL_DATABASE=images -e MYSQL_USER=developer -e MYSQL_PASSWORD=password  -v $HOME/var/lib/mysql:/var/lib/mysql -p 3306:3306 mysql:latest


download the image and run the image:
docker pull ghcr.io/chatgut/imageservice:main

## Endpoints

The following endpoints are available:



### Endpoint: POST /images


Request Parameters:
- `image` - The image file to upload.

Returns a string with a url to the image;

Example:

- input: `image - examplePic.png`

- output: `http://localhost:8001/images/1`
- url is auto generated, if running with a gateway, output would looks something like `http://foo:8080/images/1`

--- 
### Endpoint: GET /images/{id}


Request Parameters:
- `url - http://localhost:8001/images/1`

returns the image. Currently content-Type is image/png

Example:

- input: `http://localhost:8001/images/1`

- output: `examplePic.png`
--- 
### Endpoint: GET /images/thumbnail/{id}

This endpoint takes an existing image already uploaded and returns it as a resized image. Main use is for creating thumbnails.

Request Parameters:
- `url - http://localhost:8001/images/thumbnail/1`
- url paramaters is  `height` and `width` any integer value is accepted.

Example:

- input: `http://localhost:8001/images/thumbnail/8?height=200&width=200`

- output: `examplePic.png` (resized to 200x200 pixels.

--- 
# Responses
#### `POST localhost:8080/images`
- `200 OK` if image is uploaded

#### `GET localhost:8080/images/{id}`
- `200 OK` if there is a image to return
- `404 NOT FOUND`if there is no matching image

#### `GET localhost:8080/images/thumbnail/{id}`
- `200 OK` if there is an existing image already uploaded
- `404 NOT FOUND`if there is no matching image

