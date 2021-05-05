<template>
  <div></div>
</template>

<script>
export default {
  mounted() {
    const details = this.$store.state.user.details
    let role = this.$store.state.user.role
    if (!role && details && details.roles) {
      role = "admin"
      this.$store.commit('user/saveLoginRole', role)
    }
    if (role === "admin") {
      this.$router.replace("/commodity")
    } else {
      this.$router.replace("/user/profile")
    }
  }
}
</script>
