<template>
  <div class="w-full h-full">
    <div class="shadow-md mb-8">
      <div class="container mx-auto py-4 px-4 flex gap-4">
        <div class="align-middle">
          <router-link to="/" class="text-2xl mr-1">PLUTOS</router-link>
          <span class="text-xs bg-blue-300 px-1 py-0.5 rounded align-top">BETA</span>
        </div>

        <div class="flex-grow"></div>

        <div class="inline-block flex items-center text-xl text-gray-500 hover:text-black">
          <router-link to="/statements">Statements</router-link>
        </div>

        <div class="inline-block flex items-center text-xl text-gray-500 hover:text-black">
          <router-link to="/accounts">Accounts</router-link>
        </div>

        <div class="inline-block flex items-center text-xl text-gray-500 hover:text-black">
          <router-link to="/categories">Categories</router-link>
        </div>

        <div class="inline-block flex items-center text-xl text-gray-500 hover:text-black">
          <router-link to="/tags">Tags</router-link>
        </div>
      </div>
    </div>

    <div class="container mx-auto py-4 px-4">
      <router-view></router-view>
    </div>

    <div class="fixed top-0 bottom-0 left-0 right-0" style="background-color: rgba(0, 0, 0, 0.25)" v-if="$store.state.loading > 0">
      <div class="grid content-center w-full h-full">
        <div class="mx-auto">
          <font-awesome-icon class="text-5xl text-gray-100 animate-spin" icon="fa-solid fa-circle-notch"></font-awesome-icon>
        </div>
      </div>
    </div>

    <ToastMessages></ToastMessages>
  </div>
</template>

<script>
import axios from "axios";
import ToastMessage from "./components/ToastMessage.vue";
import ToastMessages from "./components/ToastMessages.vue";
import Modal from "./components/Modal.vue";

export default {
  name: "App",
  components: {Modal, ToastMessages, ToastMessage},
  data() {
    return {
      failedAttempts: 0
    }
  },
  mounted() {
    setInterval(() => {
      this.keepAlive()
    }, 2500)
  },
  methods: {
    keepAlive() {
      axios.post(this.$store.state.baseUrl + "/utils/keepalive").then(() => {
        this.failedAttempts = 0
      }).catch(() => {
        this.failedAttempts += 1
        if (this.failedAttempts < 3) return
        window.close()
      })
    }
  }
}
</script>

<style scoped>
</style>
