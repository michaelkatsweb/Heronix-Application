# Remove lines with setCreatedAt
sed -i '/setCreatedAt/d' ReportShareService.java
sed -i '/setUpdatedAt/d' ReportShareService.java
sed -i '/setActive/d' ReportShareService.java
sed -i '/setRevokedAt/d' ReportShareService.java

sed -i '/setCreatedAt/d' ReportScheduleConfigService.java
sed -i '/setUpdatedAt/d' ReportScheduleConfigService.java
sed -i '/setEnabled/d' ReportScheduleConfigService.java

sed -i '/setUpdatedAt/d' ReportTemplateService.java

sed -i '/getMonitorName/d' ReportMonitorService.java
sed -i '/setHealthy/d' ReportMonitorService.java
sed -i '/setHealthScore/d' ReportMonitorService.java
