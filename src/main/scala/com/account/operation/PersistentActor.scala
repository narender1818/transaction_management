package com.account.operation

import java.util.Date

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

object PersistentActors extends App {
  
  case class Invoice(recipient: String, data: Date, amount: Int)
  case class InvoiceRecords(id: Int, recipient: String, data: Date, amount: Int)

  class Accountant extends PersistentActor with ActorLogging {
    var latestInvoiceId = 0
    var totalamount = 0

    override def persistenceId: String = "simple-accountant"

    override def receiveCommand: Receive = {
      case Invoice(recipient, data, amount) => {
       
        log.info(s" Receive invoice for amount :$amount")
        val events = InvoiceRecords(latestInvoiceId, recipient, data, amount)
        persist(events) { e =>
          latestInvoiceId += 1
          totalamount += amount
          log.info(s"Persisted $e as event #${e.id}, for total amount $totalamount ")

        }
      }
    }
    override def receiveRecover: Receive = {
      case InvoiceRecords(id, _, _, amount) => {
        latestInvoiceId = id
        totalamount = amount
        log.info(s"Recoverd invoice #$id, for amount $amount , total amount $totalamount")
      }
    }
  }
  val system = ActorSystem("PersistentActors")
  val accountant = system.actorOf(Props[Accountant], "Accountant")
//  for (i <- 1 to 10) {
//    accountant ! Invoice("sdds", new Date(), 1 * 100)

 // }
}
