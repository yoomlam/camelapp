# Inspired from https://www.cloudbees.com/blog/writing-microservice-in-ruby
require 'bunny'
require 'json'
require 'health_data_assessor'
require 'pry'

class RabbitSubscriber

  def initialize()
    @connection = Bunny.new
    @connection.start

    setup_queue(exchange_name: 'assess_health_data', queue_name: 'health_data_assessor')
  end

  CAMEL_MQ_PROPERTIES = { durable: true, auto_delete: true }

  def setup_queue(exchange_name:, queue_name:)
    channel = @connection.create_channel
    # Exchange and queue properties must match those set up in Camel
    # http://rubybunny.info/articles/exchanges.html
    @exchange = channel.direct(exchange_name, CAMEL_MQ_PROPERTIES)
    # in case message cannot be delivered
    @exchange.on_return do |return_info, properties, content|
      puts "Got a returned message: #{content}"
    end

    @queue = channel.queue(queue_name, CAMEL_MQ_PROPERTIES)
    # https://www.baeldung.com/java-rabbitmq-exchanges-queues-bindings
    @queue.bind(exchange_name, routing_key: queue_name)
  end

  def subscribe()
    begin
      puts ' [*] Waiting for messages. To exit press CTRL+C'
      @queue.subscribe(block: true) do |delivery_info, properties, body|
        puts " [x] Received body with size: #{body.size}"
        puts "reply_to: #{properties.reply_to}"
        puts "correlation_id: #{properties.correlation_id}"
        puts "Headers: #{properties.headers}"
        puts "delivery_info: #{delivery_info}"
        puts delivery_info.consumer_tag # => a string
        puts delivery_info.redelivered? # => false
        puts delivery_info.delivery_tag # => 1
        puts delivery_info.routing_key  # => queue name
        puts delivery_info.exchange     # => ""

        json = JSON.parse(body)
        response = HealthDataAssessor.new.assess(json['contention'], json['bp_observations'])
        puts "Response: #{response}"
        # response.bytes
        @exchange.publish(
          response.to_json,
          routing_key: properties.reply_to,
          correlation_id: properties.correlation_id
        )
      end
    rescue Interrupt => _
      @connection.close

      exit(0)
    end
  end
end
