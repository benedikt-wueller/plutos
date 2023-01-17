<template>
  <div class="fixed top-0 bottom-0 left-0 right-0" style="background-color: rgba(0, 0, 0, 0.5)" @click.self="hide">
    <div class="container mx-auto py-4 px-4 grid content-center h-full" @click.self="hide">
      <div class="shadow-xl mx-auto bg-white w-full md:w-2/3 lg:w-1/2 py-4 px-5 rounded-md max-h-full overflow-y-auto">
        <div class="flex mb-4">
          <div class="text-xl font-semibold flex-grow">{{ title }}</div>
          <a class="text-xl cursor-pointer text-gray-300 hover:text-gray-500" @click="hide">
            <font-awesome-icon icon="fa-solid fa-times"></font-awesome-icon>
          </a>
        </div>

        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Modal",
  props: ['title'],
  methods: {
    hide() {
      this.$emit('hide')
    }
  }
}
</script>

<style scoped>

</style>
