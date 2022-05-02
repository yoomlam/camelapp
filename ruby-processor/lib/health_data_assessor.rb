class HealthDataAssessor
  def assess
    transformed_data = {
      effectiveDateTime: Time.now.to_s,
      practitioner: 'DR. THOMAS359 REYNOLDS206 PHD',
      organization: 'LYONS VA MEDICAL CENTER',
      systolic: {
        'code' => '8480-6',
        'display' => 'Systolic blood pressure',
        'value' => 175.0,
        'unit' => 'mm[Hg]'
      },
      diastolic: {
        'code' => '8462-4',
        'display' => 'Diastolic blood pressure',
        'value' => 111.0,
        'unit' => 'mm[Hg]'
      }
    }
    {
      "submission_id" => 987,
      "contention" => "hypertension",
      "sufficient_evidence" => true,
      "assessed_data" => {
        "bp_readings" => transformed_data
      }
    }
  end
end
