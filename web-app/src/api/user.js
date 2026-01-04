import request from "@/utils/request";

export function RegisterUser(data) {
  return request({
    url: "/user/register",
    method: "post",
    data
  });
}

export function SendUserEmail(email) {
  return request({
    url: "/user/sendEmail",
    method: "get",
    params: {email}
  });
}

export function LoginUser(data) {
  return request({
    url: "/user/login",
    method: "post",
    data
  });
}

export function FindAllUsers() {
  return request({
    url: "/admin/users",
    method: "get"
  });
}

export function UpdateUserStatus(id, disabled) {
  return request({
    url: `/admin/users/${id}/status`,
    method: "put",
    params: {disabled}
  });
}
