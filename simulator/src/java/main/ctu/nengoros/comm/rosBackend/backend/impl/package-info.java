package ctu.nengoros.comm.rosBackend.backend.impl;

/**
 * 
 * This package contains so-called ROS backends. 
 * Backend is here a class which directly handles ROS messages and converts them into 
 * Nengo data. Incomming and outgoing ROS messages can be converted from arrays of floats
 * (so far) by an arbitrary convertor ( @see nengoros.comm.transformations ). 
 *
 * Backend is able to convert data both ways Nengo->ROS and ROS->Nengo. 
 * 
 * There are two main kinds of backends: backend with data dimensions given by the ROS message
 * deffinition, or backend with user-defined data dimension (e.g. Foat32multiarray).
 * 
 * Note: in order to add custom backend, create class in backend.impl AND add new data type 
 * description in backend.DataTypesMap and most importantly: add its constructor to 
 * class backend.BackendUtils!
 * 
 * @author Jaroslav Vitku
 *
 */
