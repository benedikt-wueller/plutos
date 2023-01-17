<template>
  <div class="flex w-full max-w-xs p-4 text-gray-500 bg-white rounded-lg shadow-md border border-gray-100 pointer-events-auto">
    <div :class="'inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-' + color + '-500 bg-' + color + '-100 rounded-lg'">
      <font-awesome-icon :icon="icon"></font-awesome-icon>
    </div>
    <div class="ml-3 text-sm font-normal flex items-center flex-grow">
      <div>{{ message }}</div>
    </div>
    <button type="button" class="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-gray-500 bg-white rounded-lg hover:bg-gray-100" @click="this.$emit('close')">
      <font-awesome-icon icon="fa-solid fa-xmark"></font-awesome-icon>
    </button>
  </div>
</template>

<script>
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default {
  name: "ToastMessage",
  components: {FontAwesomeIcon},
  props: {
    type: String,
    message: String
  },
  computed: {
    icon() {
      switch (this.type) {
        case 'success': return 'fa-solid fa-check'
        case 'warning': return 'fa-solid fa-triangle-exclamation'
        case 'error': return 'fa-solid fa-xmark'
      }
    },
    color() {
      switch (this.type) {
        case 'success': return 'green'
        case 'warning': return 'orange'
        case 'error': return 'red'
      }
    }
  }
}
</script>

<style scoped>

</style>