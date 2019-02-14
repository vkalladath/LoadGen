# LoadGen
Spring boot application for load testing

This app returns a random aplhanumeric string of configured size.

Configuration is done using Environment variables.

    MIN_LATENCY - Default 0
      Minimum latency in milliseconds introduced when responding.
      
    MAX_LATENCY - Default 0
      Maximum latency in milliseconds introduced when responding.
      If min and max latencies are same, then all the requests will have the same latency. If different(max>min), a random number between min and max will be used. 

    RESPONSE_SIZE - Default 1024
      Total number of bytes returned. A few additional bytes are returned at the end to show the total number of requests recieved.
      
TODO:
 - Runtime parameters to request for different response size and latency
 - Additional parameters for simulationg CPU/IO load
 
 
