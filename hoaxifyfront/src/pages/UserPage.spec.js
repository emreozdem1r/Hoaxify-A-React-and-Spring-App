import React from 'react'
import {render, queryByText, waitForElement} from '@testing-library/react'
import UserPage from './UserPage'
import * as apiCalls from '../api/apiCalls'

const mockSucccessGetUser = {
    data:{
        id: 1,
        username: 'user1',
        displayName: 'display1',
        image: 'profile1.png'
    }
}
const mockFailGetUser = {
    response: {
        data: {
            message: 'User not found'
        }
    }
}
const match = {
    params: {
        username: 'user1'
    }
}
const setup = (props) => {
    return render(<UserPage {...props}/>)
}

describe('HomePage', () => {

    describe('Layout', () => {

        it('has root page div', () => {
            const { queryByTestId } = setup()
            const userPageDiv = queryByTestId('userpage');
            expect(userPageDiv).toBeInTheDocument()
        })
        it('displays the displayName@username when user data loaded', async() => {
            apiCalls.getUser = jest.fn().mockResolvedValue(mockSucccessGetUser);
            const { queryByText } =  setup({match})
            const text = await waitForElement(() => queryByText('display1@user1')            )
            expect(text).toBeInTheDocument()
        })
        it('displays not found alert when user not found', async() => {
            apiCalls.getUser = jest.fn().mockRejectedValue(mockFailGetUser);
            const { queryByText } =  setup({match})
            const alert = await waitForElement(() => queryByText('User not found')            )
            expect(alert).toBeInTheDocument()
        })
        it('displays spinner while loading user data', () => {
            const mockDelayedResponse = jest.fn().mockImplementation(() =>{
                return new Promise((promise, reject) => {
                    setTimeout(() => {
                        resolve(mockSucccessGetUser)
                    },300)
                })
            })
            apiCalls.getUser = mockDelayedResponse
            const { queryByText } =  setup({ match })
            const spinner = queryByText('Loading...')
            expect(spinner).toBeInTheDocument()
        })
        
    })
    describe('Lifecycle', () => {

        it('calls getUser when it is rendered', () => {
            apiCalls.getUser = jest.fn().mockResolvedValue(mockSucccessGetUser);
            setup({match})
            expect(apiCalls.getUser).toHaveBeenCalledTimes(1);
        })
        it('calls getUser for user1 when it is rendered with user1 in match', () => {
            apiCalls.getUser = jest.fn().mockResolvedValue(mockSucccessGetUser);
            setup({match})
            expect(apiCalls.getUser).toHaveBeenCalledWith("user1");
        })
        
    })
})