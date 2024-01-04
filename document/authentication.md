# Authentication and Authorization Flow

The authentication is handled by Spring Security where we are making use of AuthenticationProvider and
AuthenticationManager. When a user signs up, they can either be assigned a role CLIENT or (CLIENT AND WORKER).
It all depends on the route used. If a user sings up from the storefront, they would be assigned a role CLIENT else
if user signs up using the admin route, roles CLIENT and WORKER are assigned. The catch signing up using the admin
route, a valid json web token has to be present in the request. JWT has to be present in the cookie of a custom name
JSESSIONID.
RegisterDTO and LoginDTO are the objects responsible for handling collecting the users information.

## Type of Authentication
As mentioned earlier, we are taking advantage of authentication using jwt. The authentication flow goes as a user makes
a call to either `api/v1/client/auth/login` or `api/v1/worker/auth/login`. After that we rely on Spring Security to
validate the payload with what is saved in the database. If everything checkouts, a custom cookie is sent back in the
response. This cookie contains a http only json web token (jwt) is sent back in the response.

