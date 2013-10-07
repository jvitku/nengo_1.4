/**
 * This package is used mainly for testing the possibilities to 
 * parse the type and dimensions of ROS message payloads 
 */
/**
 * @author Jaroslav Vitku
 *
 */

/**
 * from rosjava_messges: std_msg.. 
 * --------------------------------------------------------------------
 * MULTIARRAY LAYOUT doc (from rosjava_messages/std_msgs/)
 * 
 *  # The multiarray declares a generic multi-dimensional array of a
 *  # particular data type.  Dimensions are ordered from outer most
 *  # to inner most.
 *  #
 *  MultiArrayDimension[] dim 
 *  # Array of dimension properties
 *  uint32 data_offset        
 *  # padding bytes at front of data
 *  #
 *  # Accessors should ALWAYS be written in terms of dimension stride
 *  # and specified outer-most dimension first.
 *  # 
 *  # multiarray(i,j,k) = data[data_offset + dim_stride[1]*i + dim_stride[2]*j + k]
 *  #
 *  # A standard, 3-channel 640x480 image with interleaved color channels
 *  # would be specified as:
 *  #
 *  # dim[0].label  = "height"
 *  # dim[0].size   = 480
 *  # dim[0].stride = 3*640*480 = 921600  (note dim[0] stride is just size of image)
 *  # dim[1].label  = "width"
 *  # dim[1].size   = 640
 *  # dim[1].stride = 3*640 = 1920
 *  # dim[2].label  = "channel"
 *  # dim[2].size   = 3
 *  # dim[2].stride = 3
 *  #
 *  # multiarray(i,j,k) refers to the ith row, jth column, and kth channel.";
 *  
 *  ----------------------------------------------------------------------
 *  MULTIARRAY DIMENSION doc (from rosjava_messages/std_msgs/)
 *  string label   
 *  label of given dimension
 *  uint32 size    
 *  
 *  # size of given dimension (in type units)
 *  uint32 stride  
 *  # stride of given dimension";
 */
package resender.mpt;


