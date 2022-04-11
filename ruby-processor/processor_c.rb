#!/usr/bin/env ruby
require 'bunny'
require 'json'

connection = Bunny.new
connection.start


channel = connection.create_channel

# Exchange and queue properties must match those set up in Camel
ex = channel.exchange('claimTypeC', type: 'direct',
    durable: true, auto_delete: true)
queue = channel.queue('processor_c.rb',
    durable: true, auto_delete: true)
channel.queue_bind('processor_c.rb', exchange='claimTypeC')

begin
  puts ' [*] Waiting for messages. To exit press CTRL+C'
  queue.subscribe(block: true) do |_delivery_info, properties, body|
    puts " [x] Received #{body}"
    # puts " => #{body.pack('C*')}"
    response = {
      "createdAt" => 1644643358000,
      "submission_id" => "subm123",
      "resultStatus" => "SUCCESS",
      "results" => {
        "bp_diastolic" => 75,
        "rrd_pdf_path" => "rrd/hypertension/subm123.pdf",
        "p_systolic" => 100
      }
    }
    puts "Response: #{response}"
    # response.bytes
    ex.publish(
      response.to_json,
      routing_key: properties.reply_to,
      correlation_id: properties.correlation_id
    )
  end
rescue Interrupt => _
  connection.close

  exit(0)
end
