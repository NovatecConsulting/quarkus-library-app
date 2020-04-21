package library.service.messaging

import library.service.business.books.domain.events.BookEvent
import library.service.business.events.EventDispatcher
import javax.inject.Singleton

@Singleton
class MessagingBookEventDispatcher(
        //private val rabbitTemplate: RabbitTemplate,
        //private val exchange: BookEventsExchange,
        //private val postProcessor: CorrelationIdMessagePostProcessor,
        //private val eventCounter: DomainEventSendCounter
) : EventDispatcher<BookEvent> {

    //private val log = MessagingBookEventDispatcher::class.logger

    override fun dispatch(event: BookEvent) {
        println("dispatching event [{}] to exchange [{}]")
        //rabbitTemplate.convertAndSend(exchange.name, event.type, event, postProcessor)
        //eventCounter.increment(event)
    }

}