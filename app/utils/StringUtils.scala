package utils

object StringUtils {
    
    def isEmpty(s: String): Boolean = {
        if (s.length == 0) {
            true
        }else{
            false
        }
    }
    
    def optionalString(s: String): Option[String] = {
        if(!StringUtils.isEmpty(s)){
            Some(s)
        }else{
            None
        }
    }
    
    def optionalBoolean(bool: Boolean): Option[Boolean] = {
        if(bool){
            Some(true)
        }else{
            None
        }
    }
}