(
  $localization := {
    "label.date.from": "From",
    "label.date.to": "To"
  };

  $transformedData := data
    .{
      "data": {
        "title": name,
        "description": description,
        "captions": [
          {
            "text": $lookup($localization, "label.date.from") & " " &
            $fromMillis($toMillis(eventFromDate), "[MNn] [D1o], [Y0001]") &
            " " & $lookup($localization, "label.date.to") & " " &
            $fromMillis($toMillis(eventToDate), "[MNn] [D1o], [Y0001]")
          },
          {
            "text": $fromMillis($toMillis(eventFromDate), "[h]:[m01] [P]") & " " &
            $lookup($localization, "label.date.to") & " " &
            $fromMillis($toMillis(eventToDate), "[h]:[m01] [P]")
          }
        ]
      },
      "metadata": {}
    };

  {
    "status": "success",
    "data": {
      "list": $type($transformedData) = "array" ? $transformedData : [$transformedData],
      "filters": data.filters
    }
  }
)