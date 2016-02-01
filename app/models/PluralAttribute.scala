package models

trait PluralAttribute {
    
    def value: String
    
    def `type`: String
    
    def primary: Option[Boolean]
}