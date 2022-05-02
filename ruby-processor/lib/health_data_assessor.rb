require 'lighthouse_observation_data'

class HealthDataAssessor
  def assess(contention, bp_observations)
    case contention
    when 'hypertension'
      LighthouseObservationData.new(bp_observations).transform
    else
      { error: "Unsupported contention: #{contention}" }
    end
  end
end
