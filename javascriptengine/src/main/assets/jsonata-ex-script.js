(
$language := config.lang;
$localization := config.localization;
$formatDate := function($d) { $fromMillis($toMillis($d, "[Y0001]-[M01]-[D01]"), "[D] [MNn] [Y0001]") };

/* 1) Normalize executionDate -> yyyy-mm-dd */
$headerDates := $distinct(data.list[].executionDate.($substring($, 0, 10)));

/* 2) Headers */
$headers := $headerDates.({
   "headerId": $,
   "component": "Text",
   "props": {
     "text": $lookup($localization, "label.scheduledFor") & " " & ($),
     "style": { "fontStyle": "h5" },
     "multiline": false
   }
});

/* 3) Cards */
$cards := data.list[].(
   $headerId := $substring(executionDate, 0, 10);
   payments[].{
     "headerId": $headerId,
     "props": {
       "thumbnail": { "light": logo, "dark": logo },
       "title": title,
       "caption1": entityName,
       "caption": desc,
       "selection": "none",
       "statusText":
         daysLeft > 0 and daysLeft <= 5 ?
         { "text": ($lookup($localization, "label.due") & " " & daysLeft & " " & $lookup($localization, "label.days")), "type": "warning" }
         : undefined,
       "items": [ { "label": $lookup($localization, "label.scheduledAmount"), "value": { "text": subscript = 0 ? $lookup($localization,"label.fullAmount"): $string(subscript) } } ],
       "onClick": { "actionable": { "actionType": "navigation", "data": { "link": "/wb/dge/autoGov/manage-payment/" & (id ? id : "") } } }
     }
   }
);

/* 4) Dropdowns - Dynamic Keys and IDs */
$entityValuesRaw := data.lookup.filters.entities.({ "ID": id, "Name": name, "Logo": logo });
/* de-duplicate by ID */
$distinctIDs := $distinct($entityValuesRaw.ID);
$entityValues := $distinctIDs.(
   $distinctID := $;
   $entityValuesRaw[$distinctID = ID][0]
);

$paymentTypeValues := data.lookup.filters.paymentTypes.({ "ID": id, "Name": name });
$statusValues := data.lookup.filters.statuses.({ "ID": id, "Name": name });

/* Generate dropdowns dynamically from API response */
$dropdowns := data.lookup.filters ? $each(data.lookup.filters, function($v, $k) {
    {
      "Key": $k,
      "ID": $k,
      "Name": $lookup($localization, "label."&$k&"Filter"),
      "Values": [$v.({ "ID":id, "Name": name })]
    }
  });

$lookups := {
   "dropdowns": [$dropdowns,{
     "Key": "date",
     "ID": "date",
     "type": "dateRange",
     "Name": $lookup($localization, "label.dateFilter")
   }]
};

{
   "status": "success",
   "data": {
     "headers": $type($headers) = "array" ? $headers : [$headers],
     "list": $type($cards) = "array" ? $cards : [$cards],
     "filters": $lookups
   }
}
)
