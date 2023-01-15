<script setup>
defineProps({
  transaction: Object,
  showAccount: Boolean
})

function formatNumber(number) {
  return parseFloat(number).toFixed(2)
}
</script>

<template>
  <div class="block p-6 bg-white border border-gray-200 rounded-lg shadow-md hover:bg-gray-100 grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
    <div>
      <div class="font-semibold text-md">{{ transaction.thirdPartyName || transaction.type }}</div>
      <div v-if="transaction.purpose" class="font-normal text-gray-700 mt-0.5">{{ transaction.purpose }}</div>
      <div v-if="transaction.comment" class="font-normal text-gray-700 mt-0.5">{{ transaction.comment }}</div>
      <div class="mt-2">
        <span class="py-0.5 px-2 bg-gray-200 rounded-xl pt-1">{{ transaction.category == null ? 'Other' : transaction.category.name }}</span>
      </div>
    </div>
    <div>
      <div class="text-right">
        <div :class="{
          'text-lg font-semibold hover:opacity-100': true,
          'text-red-500': transaction.amount < 0,
          'opacity-50': transaction.valuationDate > new Date().toISOString() }">{{ formatNumber(transaction.amount) }} {{ transaction.currency }}</div>
        <div>{{ transaction.type }}</div>
        <div v-if="showAccount && transaction.account">{{ transaction.account.name }}</div>
        <div>
          <span v-if="transaction.bookkeepingDate !== transaction.valuationDate">
            {{ transaction.bookkeepingDate }} <span :class="{ 'opacity-50': transaction.valuationDate > new Date().toISOString() }">({{ transaction.valuationDate }})</span>
          </span>
          <span v-if="transaction.bookkeepingDate === transaction.valuationDate">
            {{ transaction.bookkeepingDate }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
