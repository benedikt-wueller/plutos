<template>
  <div class="block bg-white border border-gray-200 rounded-lg shadow hover:bg-gray-100 overflow-hidden border"
       :style="{ borderColor: category.attributes.color }">
    <div class="text-md font-semibold px-2 py-1.5 bg-gray-100 text-center"
         :style="{ 'background-color': category.attributes.color, color: category.attributes.textColor }">
      <span>{{ category.attributes.name }}</span>
      <span v-if="category.attributes.default" class="ml-1 opacity-30">(Default)</span>
    </div>

    <div class="p-4">
      <div class="flex">
        <div class="font-semibold text-md text-gray-700 flex-grow">
          <span v-if="total < 0" class="text-red-500">{{ $filters.formatNumber(total) }} {{ category.attributes.currency }}</span>
          <span v-else class="text-md">{{ $filters.formatNumber(total) }} {{ category.attributes.currency }}</span>
        </div>
        <div class="text-right">
          <span v-if="budget">{{ $filters.formatNumber(budget) }} {{ category.attributes.currency }}</span>
          <span v-if="budget && Math.abs(budgetFactor - 1.0) > 0.01"> ({{ category.attributes.limit }} &times; {{ $filters.formatNumber(budgetFactor) }})</span>
          <span v-if="!budget">No Limit</span>
        </div>
      </div>

      <div class="mt-2 mb-4" v-if="budget">
        <div class="shadow w-full bg-gray-100 rounded-full overflow-hidden">
          <div class="text-xs leading-none py-1 text-center text-white rounded-full overflow-hidden"
               :class="this.color"
               :style="'width: ' + Math.max(0, Math.min(100, percent)) + '%'">{{ $filters.formatNumber(percent) }}%</div>
        </div>
      </div>

      <div v-if="tags.length > 0" class="mt-2">
        <div v-for="tag in tags" v-bind:key="'tag-' + tag.id"
             class="inline-block rounded-full py-0.5 px-2 cursor-pointer hover:opacity-100 mr-1 mb-0.5 text-xs"
             :style="{ 'background-color': tag.attributes.color, color: tag.attributes.textColor }">
          <span>{{ tag.attributes.name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Category",
  props: {
    category: Object,
    statements: Array,
    budgetFactor: Number
  },
  computed: {
    budget() {
      const limit = this.category.attributes.limit
      if (limit === undefined || limit === null) return null;
      return limit * this.budgetFactor
    },
    total() {
      let sum = 0
      this.statements.forEach(it => sum += it.attributes.amount)
      return sum
    },
    percent() {
      if (!this.budget) return 0
      return Math.round(-this.total / this.budget * 10000) / 100
    },
    color() {
      if (this.percent < 75) {
        return 'bg-green-500'
      } else if (this.percent < 100) {
        return 'bg-orange-500'
      } else {
        return 'bg-red-500'
      }
    },
    tags() {
      const tagIds = this.category.relationships.tags.data.map(it => it.id)
      return this.$store.getters.getTags().filter(it => tagIds.includes(it.id))
    }
  }
}
</script>

<style scoped>

</style>