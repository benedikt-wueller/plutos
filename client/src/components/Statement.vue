<template>
  <div v-if="category && account" class="block p-6 bg-white border border-gray-200 rounded-lg shadow-md grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
    <div>
      <div class="font-semibold text-md">{{ statement.attributes.thirdPartyName || statement.attributes.type }}</div>
      <div v-if="statement.attributes.purpose" class="font-normal text-gray-700 mt-0.5">{{ statement.attributes.purpose }}</div>
      <div v-if="statement.attributes.comment" class="font-normal text-gray-700 mt-0.5">{{ statement.attributes.comment }}</div>
      <div class="mt-1">
        <div class="inline-block py-0.5 px-2.5 rounded-full mr-1" :style="{ 'background-color': category.attributes.color, color: category.attributes.textColor }">
          <span>{{ category.attributes.name }}</span>
        </div>
        <div v-for="tag in getStatementTags(statement)" class="inline-block py-0.5 px-2.5 rounded-full mr-1 bg-gray-200 text-black">
          <font-awesome-icon icon="fa-sold fa-tag"></font-awesome-icon>
          <span class="ml-1">{{ tag.attributes.name }}</span>
        </div>
      </div>
    </div>
    <div>
      <div class="text-right">
        <div :class="{
          'text-lg font-semibold hover:opacity-100': true,
          'text-red-500': statement.attributes.amount < 0,
          'opacity-50': statement.attributes.valueDate > new Date().toISOString() }">{{ $filters.formatNumber(statement.attributes.amount) }} {{ statement.attributes.currency }}</div>

        <div>{{ statement.attributes.type }}</div>
        <div>{{ account.attributes.name }}</div>

        <div v-if="!statement.attributes.bookingDate">
          <span>{{ statement.attributes.valueDate }}</span>
        </div>
        <div v-if="statement.attributes.bookingDate">
          <span v-if="statement.attributes.bookingDate !== statement.attributes.valueDate">
            {{ statement.attributes.bookingDate }} <span :class="{ 'opacity-50': statement.attributes.valueDate > new Date().toISOString() }">({{ statement.attributes.valueDate }})</span>
          </span>
          <span v-if="statement.attributes.bookingDate === statement.attributes.valueDate">
            {{ statement.attributes.bookingDate }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default {
  name: "Statement",
  components: {FontAwesomeIcon},
  props: {
    statement: Object
  },
  computed: {
    account() {
      return this.$store.getters.findAccount(this.statement.relationships.account.data.id)
    },
    category() {
      return this.$store.getters.findCategory(this.statement.relationships.category.data.id)
    }
  },
  methods: {
    getStatementTags(statement) {
      const tagIds = statement.relationships.tags.data.map(it => it.id)
      return this.$store.getters.getTags().filter(it => tagIds.includes(it.id))
    }
  }
}
</script>
