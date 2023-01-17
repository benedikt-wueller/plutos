<template>
  <div class="fixed top-0 bottom-0 left-0 right-0 pointer-events-none">
    <div class="py-5 px-4 flex gap-4 items-end flex flex-col h-full">
      <div class="flex-grow"></div>
      <ToastMessage v-for="(toast, index) in toasts" v-bind:key="'toast-' + index" :type="toast.type" :message="toast.message" @close="removeToast(index)"></ToastMessage>
    </div>
  </div>
</template>

<script>
import ToastMessage from "../components/ToastMessage.vue";

export default {
  name: "ToastMessages",
  components: {ToastMessage},
  computed: {
    toasts() {
      return this.$store.state.toasts
    }
  },
  methods: {
    removeToast(index) {
      this.$store.commit('removeToast', { index })
    }
  }
}
</script>

<style scoped>

</style>
