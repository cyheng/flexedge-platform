<template>
    <li :class="{'removed': item.checked}" class="list-group-item">
        <div class="checkbox">
            <label>
                <input v-model="item.checked" type="checkbox">
                <span>{{ item.text }}</span>
            </label>
        </div>
    </li>
</template>

<script>
export default {
    props: ['item']
}
</script>

<style>
.removed {
    color: gray;
}

.removed span {
    text-decoration: line-through;
}
</style>