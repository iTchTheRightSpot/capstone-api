package dev.webserver.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "user")
@Builder
public class User {

        @Id
        @Column("user_id")
        private final Long userId;
        @NotNull(message = "user firstname cannot be null")
        @NotEmpty(message = "user firstname cannot be empty")
        @Size.List({
                @Size(min = 100, message = "user firstname max length of 100"),
                @Size(max = 100, message = "user firstname max length of 100")
        })
        private final String firstname;
        @NotNull(message = "user fullname cannot be null")
        @NotEmpty(message = "user fullname cannot be empty")
        @Size.List({
                @Size(min = 255, message = "user fullname max length of 255"),
                @Size(max = 255, message = "user fullname max length of 255")
        })
        private final String fullname;
        @NotNull(message = "user fullname cannot be null")
        @NotEmpty(message = "user fullname cannot be empty")
        @Size.List({
                @Size(min = 255, message = "user fullname max length of 255"),
                @Size(max = 255, message = "user fullname max length of 255")
        })
        private final String email;
        @Size.List({
                @Size(min = 255, message = "user image_key max length of 255"),
                @Size(max = 255, message = "user image_key max length of 255")
        })
        @Column("image_key")
        @Setter
        private String imageKey;

        @JsonProperty("user_id")
        public Long userId() {
                return userId;
        }

        @JsonProperty("firstname")
        public String firstname() {
                return firstname;
        }

        @JsonProperty("fullname")
        public String fullname() {
                return fullname;
        }

        @JsonProperty("email")
        public String email() {
                return email;
        }

        @JsonProperty("image_key")
        public String imageKey() {
                return imageKey;
        }
}
