package models

trait PluralAttribute {

  def value: Option[String]

  def `type`: Option[String]

  def primary: Option[Boolean]
}