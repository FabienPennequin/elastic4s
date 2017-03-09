package com.sksamuel.elastic4s.searches

import cats.syntax.either._
import com.sksamuel.elastic4s.HitReader
import com.sksamuel.elastic4s.searches.aggs.RichAggregations
import com.sksamuel.elastic4s.searches.suggestions._
import org.elasticsearch.action.search.{SearchResponse, ShardSearchFailure}
import org.elasticsearch.search.SearchHits

import scala.concurrent.duration._

case class RichSearchResponse(original: SearchResponse) {

  def size: Int = original.getHits.hits().length
  def ids: Seq[String] = hits.map(_.id)
  def totalHits: Long = original.getHits.getTotalHits
  def maxScore: Float = original.getHits.getMaxScore

  def hits: Array[RichSearchHit] = original.getHits.getHits.map(RichSearchHit.apply)

  def to[T: HitReader]: IndexedSeq[T] = safeTo.flatMap(_.toOption)
  def safeTo[T: HitReader]: IndexedSeq[Either[Throwable, T]] = hits.map(_.safeTo[T]).toIndexedSeq

  def scrollId: String = original.getScrollId
  def scrollIdOpt: Option[String] = Option(scrollId)

  def totalShards: Int = original.getTotalShards
  def successfulShards: Int = original.getSuccessfulShards
  def shardFailures: Array[ShardSearchFailure] = Option(original.getShardFailures).getOrElse(Array.empty)

  def tookInMillis: Long = original.getTookInMillis
  def took: Duration = original.getTookInMillis.millis

  def aggregations: RichAggregations = RichAggregations(original.getAggregations)

  def isEmpty: Boolean = hits.isEmpty
  def nonEmpty: Boolean = hits.nonEmpty

  def suggest: SuggestResult = SuggestResult(original.getSuggest)
  def suggestions = suggest.suggestions
  def suggestion(name: String): SuggestionResult = suggest.suggestions.find(_.name == name).get

  def termSuggestion(name: String): TermSuggestionResult = suggestion(name).asInstanceOf[TermSuggestionResult]
  def completionSuggestion(name: String) = suggestion(name).asInstanceOf[CompletionSuggestionResult]
  def phraseSuggestion(name: String): PhraseSuggestionResult = suggestion(name).asInstanceOf[PhraseSuggestionResult]

  def isTimedOut: Boolean = original.isTimedOut
  def isTerminatedEarly: Option[Boolean] = Option[java.lang.Boolean](original.isTerminatedEarly).map(_.booleanValue())

  @deprecated("use resp.aggregations, or resp.original.getAggregations", "2.0.0")
  def getAggregations = original.getAggregations

  // java aliases
  @deprecated("use suggest", "5.0.0")
  def getSuggest = original.getSuggest

  @deprecated("use scrollId or scrollIdOpt", "5.0.0")
  def getScrollId: String = original.getScrollId

  @deprecated("use hits", "5.0.0")
  def getHits: SearchHits = original.getHits

  @deprecated("use took", "5.0.0")
  def getTook = original.getTook

  @deprecated("use tookInMillis", "5.0.0")
  def getTookInMillis = original.getTookInMillis
}
