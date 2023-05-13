require 'lighthouse_observation_data'

class HealthDataAssessor
  def assess(contention, bp_observations)
    case contention
    when 'hypertension'
      return {} if medications.blank?

      transformed_medications = LighthouseObservationData.new(bp_observations).transform
      flagged_medications = transformed_medications.map do |medication|
        {
          **medication,
          flagged: ASTHMA_KEYWORDS.any? { |keyword| medication.to_s.downcase.include?(keyword) }
        }
      end
      medications = flagged_medications.sort_by { |medication| medication[:flagged] ? 0 : 1 }
      { medications: medications }
    else
      { error: "Unsupported contention: #{contention}" }
    end
  end
end
