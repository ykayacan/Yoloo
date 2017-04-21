package com.yoloo.backend.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonProperty;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PushMessage {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * This parameter specifies the recipient of a message. The value must be a registration token,
   * notification key, or topic. Do not set this field when sending to multiple topics. See
   * condition.
   */
  private String to;

  /**
   * This parameter specifies a listFeed of devices (registration tokens, or IDs) receiving a
   * multicast message. It must contain at least 1 and at most 1000 registration tokens. Use this
   * parameter only for multicast messaging, not for single recipients. Multicast messages (sending
   * to more than 1 registration tokens) are allowed using HTTP JSON format only.
   */
  @JsonProperty("registration_ids") @Singular private List<String> registrationIds;

  /**
   * This parameter specifies a logical expression of conditions that determine the message target.
   * Supported condition: Topic, formatted as "'yourTopic' in topics". This value is
   * case-insensitive. Supported operators: &&, ||. Maximum two operators per topic message
   * supported.
   */
  private String condition;

  /**
   * This parameter identifies a group of messages (e.g., with collapse_key: "Updates Available")
   * that can be collapsed, so that only the last message gets sent when delivery can be resumed.
   * This is intended to avoid sending too many of the same messages when the device comes back
   * online or becomes active. Note that there is no guarantee of the order in which messages
   * getPost sent.
   *
   * Note: A maximum of 4 different collapse keys is allowed at any given time. This means a FCM
   * connection server can simultaneously store 4 different send-to-sync messages per client app.
   * If you exceed this number, there is no guarantee which 4 collapse keys the FCM connection
   * server will keep.
   */
  @JsonProperty("collapse_key") private String collapseKey;

  /**
   * Sets the priority of the message. Valid values are "normal" and "high." On iOS, these
   * correspond to APNs priorities 5 and 10.
   *
   * By default, messages are sent with normal priority. Normal priority optimizes the client app's
   * battery consumption and should be used unless immediate delivery is required. For messages with
   * normal priority, the app may receive the message with unspecified delay.
   *
   * When a message is sent with high priority, it is sent immediately, and the app can wake a
   * sleeping device and open a network connection to your server.
   *
   * For more information,
   *
   * @see <a href="https://firebase.google.com/docs/cloud-messaging/concept-options#setting-the-priority-of-a-message"></a>}.
   */
  private PRIORITY priority;

  /**
   * This parameter specifies how long (in seconds) the message should be kept in FCM storage if the
   * device is offline. The maximum time to live supported is 4 weeks, and the default value is 4
   * weeks.
   *
   * For more information, see Setting the lifespan of a message.
   */
  @JsonProperty("time_to_live") private Integer timeToLive;

  /**
   * This parameter, when set to true, allows developers to test a request without actually sending
   * a message.
   *
   * The default value is false.
   */
  @JsonProperty("dry_run") private Boolean dryRun;

  private NotificationBody notification;

  private DataBody data;

  byte[] getJsonAsBytes() throws IOException {
    return MAPPER.writeValueAsBytes(this);
  }

  @AllArgsConstructor
  public enum PRIORITY {
    NORMAL("normal"), HIGH("high");

    String priority;
  }

  @Value
  @Builder
  public static class NotificationBody {
    private String body;
    private String title;
    private String icon;
  }

  @Value
  @Builder
  public static class DataBody {
    @Singular private Map<String, String> values;

    @JsonAnyGetter
    public Map<String, String> getValues() {
      return values;
    }
  }
}
